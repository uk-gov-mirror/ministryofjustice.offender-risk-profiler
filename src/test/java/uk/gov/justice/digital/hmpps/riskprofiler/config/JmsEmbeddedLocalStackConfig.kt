package uk.gov.justice.digital.hmpps.riskprofiler.config

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.CreateQueueRequest
import com.amazonaws.services.sqs.model.QueueAttributeName
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

@Configuration
@ConditionalOnProperty(name = ["sqs.provider"], havingValue = "localstack-embedded")
open class JmsEmbeddedLocalStackConfig {

  companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Bean
  open fun localStackContainer(applicationContext: ConfigurableApplicationContext): LocalStackContainer {
    log.info("Starting localstack...")
    val logConsumer = Slf4jLogConsumer(log).withPrefix("localstack")
    val localStackContainer: LocalStackContainer =
      LocalStackContainer(DockerImageName.parse("localstack/localstack:0.11.2"))
        .withServices(LocalStackContainer.Service.SQS, LocalStackContainer.Service.SNS)
        .withClasspathResourceMapping(
          "/localstack/setup-sns.sh",
          "/docker-entrypoint-initaws.d/setup-sns.sh",
          BindMode.READ_WRITE
        )
        .withEnv("HOSTNAME_EXTERNAL", "localhost")
        .withEnv("DEFAULT_REGION", "eu-west-2")
        .waitingFor(
          Wait.forLogMessage(".*All Ready.*", 1) // .withStartupTimeout(Duration.ofMinutes(10))
        )

    log.info("Started localstack.")

    localStackContainer.start()
    localStackContainer.followOutput(logConsumer)
    return localStackContainer
  }

  @Bean
  open fun awsClientForEvents(localStackContainer: LocalStackContainer): AmazonSQS = AmazonSQSClientBuilder.standard()
    .withEndpointConfiguration(localStackContainer.getEndpointConfiguration(LocalStackContainer.Service.SQS))
    .withCredentials(localStackContainer.defaultCredentialsProvider)
    .build()

  @Bean
  open fun awsDlqClientForEvents(localStackContainer: LocalStackContainer): AmazonSQS =
    AmazonSQSClientBuilder.standard()
      .withEndpointConfiguration(localStackContainer.getEndpointConfiguration(LocalStackContainer.Service.SQS))
      .withCredentials(localStackContainer.defaultCredentialsProvider)
      .build()

  @Bean
  @Primary
  open fun awsSqsClient(localStackContainer: LocalStackContainer): AmazonSQSAsync =
    AmazonSQSAsyncClientBuilder.standard()
      .withEndpointConfiguration(localStackContainer.getEndpointConfiguration(LocalStackContainer.Service.SQS))
      .withCredentials(localStackContainer.defaultCredentialsProvider)
      .build()

  @Bean("queueUrl")
  open fun queueUrl(
    @Qualifier("awsClientForEvents") awsSqsClient: AmazonSQS,
    @Value("\${sqs.events.queue.name}") queueName: String,
    @Value("\${sqs.events.dlq.queue.name}") dlqName: String
  ): String {
    return queueUrlWorkaroundTestcontainers(awsSqsClient, queueName, dlqName)
  }

  @Bean("dlqUrl")
  open fun dlqUrl(
    @Qualifier("awsDlqClientForEvents") awsSqsDlqClient: AmazonSQS,
    @Value("\${sqs.events.dlq.queue.name}") dlqName: String
  ): String {
    return awsSqsDlqClient.getQueueUrl(dlqName).queueUrl
  }

  private fun queueUrlWorkaroundTestcontainers(awsSqsClient: AmazonSQS, queueName: String, dlqName: String): String {
    val queueUrl = awsSqsClient.getQueueUrl(queueName).queueUrl
    val dlqUrl = awsSqsClient.getQueueUrl(dlqName).queueUrl
    // This is necessary due to a bug in localstack when running in testcontainers that the redrive policy gets lost
    val dlqArn = awsSqsClient.getQueueAttributes(dlqUrl, listOf(QueueAttributeName.QueueArn.toString()))

    // the queue should already be created by the setup script - but should reset the redrive policy
    awsSqsClient.createQueue(
      CreateQueueRequest(queueName).withAttributes(
        mapOf(
          QueueAttributeName.RedrivePolicy.toString() to
              """{"deadLetterTargetArn":"${dlqArn.attributes["QueueArn"]}","maxReceiveCount":"5"}"""
        )
      )
    )
    return queueUrl
  }
}
