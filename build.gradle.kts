plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.15.6"
  kotlin("plugin.spring") version "1.9.22"
  kotlin("plugin.jpa") version "1.9.22"
}

configurations {
  implementation { exclude(group = "tomcat-jdbc") }
  implementation { exclude(module = "spring-boot-graceful-shutdown") }
  all {
    exclude(group = "software.amazon.ion", module = "ion-java")
    exclude(group = "ch.qos.logback", module = "logback-core")
    exclude(group = "ch.qos.logback", module = "logback-classic")
  }
  testImplementation { exclude(group = "org.junit.vintage") }
}

configurations.all {
  resolutionStrategy {
    force("commons-beanutils:commons-beanutils:1.11.0")
  }
}

configurations.all {
  resolutionStrategy {
    force("org.apache.tomcat.embed:tomcat-embed-core:11.0.3")
  }
}

configurations.all {
  resolutionStrategy {
    force("org.springframework.boot:spring-boot:3.4.5")
  }
}

configurations.all {
  resolutionStrategy {
    force("org.springframework.security:spring-security-core:6.2.3")
  }
}

configurations.all {
  resolutionStrategy {
    force("org.springframework.security:spring-security-crypto:6.4.4")
  }
}

configurations.all {
  resolutionStrategy {
    force("org.springframework.security:spring-security-web:6.2.7")
  }
}

configurations.all {
  resolutionStrategy {
    force("org.springframework:spring-web:6.1.6")
  }
}

configurations.all {
  resolutionStrategy {
    force("org.springframework:spring-webflux:6.1.14")
  }
}

configurations.all {
  resolutionStrategy {
    force("org.springframework:spring-webmvc:6.1.14")
  }
}


dependencyCheck {
  suppressionFiles.add("suppressions.xml")
}

val awssdkVersion = "1.12.468"

dependencies {
  // or use:
  compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")

  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  implementation("org.slf4j:slf4j-simple:2.0.16")
  implementation("org.slf4j:slf4j-api:2.0.16")

  runtimeOnly("com.h2database:h2:2.1.214")
  runtimeOnly("org.flywaydb:flyway-core")
  runtimeOnly("org.postgresql:postgresql:42.7.2")
  implementation("jakarta.servlet:jakarta.servlet-api:6.0.0")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-security") {
    exclude("org.springframework.security", "spring-security-core")
    exclude("org.springframework.security", "spring-security-web")
  }
  implementation("org.springframework.security:spring-security-web:6.2.7")
  implementation("org.springframework.security:spring-security-core:6.2.3")

  implementation("org.springframework.boot:spring-boot-starter-webflux") {
    exclude("org.springframework", "spring-webflux")
  }
  implementation("org.springframework:spring-webflux:6.1.14")

  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server") {
    exclude("org.springframework.security", "spring-security-core")
    exclude("org.springframework.security", "spring-security-web")
  }
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client") {
    exclude("org.springframework.security", "spring-security-core")
    exclude("org.springframework.security", "spring-security-web")
  }

  implementation("org.springframework.boot:spring-boot-starter-data-redis")

  implementation("org.springframework.boot:spring-boot-starter-web") {
    exclude("org.apache.tomcat.embed", "tomcat-embed-core")
    exclude("org.apache.tomcat.embed", "tomcat-embed-websocket")
    exclude("org.apache.tomcat.embed", "tomcat-embed-el")
  }
  implementation("org.apache.tomcat.embed", "tomcat-embed-core", "10.1.18")
  implementation("org.apache.tomcat.embed", "tomcat-embed-websocket", "10.1.18")
  implementation("org.apache.tomcat.embed", "tomcat-embed-el", "10.1.18")

  implementation("com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.4")
  implementation("com.microsoft.azure:applicationinsights-logging-logback:2.6.4")

  // NOTE spring-boot-devtools does not currently play nicely with spring-data-redis,
  // see https://github.com/spring-projects/spring-boot/issues/11822, which claims to be fixed but is not.
  // implementation("org.springframework.data:spring-data-redis")
  // Note spring-data-redis 2.6.2 does not support Jedis 4.x
  implementation("redis.clients:jedis:5.2.0")

  implementation("org.springframework.cloud:spring-cloud-starter-aws-messaging:2.2.6.RELEASE")
  implementation("org.springframework:spring-jms:5.3.24")
  implementation("com.amazonaws:amazon-sqs-java-messaging-lib:1.1.2")

  implementation("net.javacrumbs.shedlock:shedlock-spring:5.2.0")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.42.0")

  implementation("org.springdoc:springdoc-openapi-ui:1.6.15")
  implementation("org.springdoc:springdoc-openapi-kotlin:1.6.15")
  implementation("org.springdoc:springdoc-openapi-security:1.6.15") {
    exclude(group = "org.springframework.security", module = "spring-security-core")
  }
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")

  implementation("io.jsonwebtoken:jjwt:0.12.3")

  implementation("com.opencsv:opencsv:5.10")
  implementation("commons-io:commons-io:2.16.1")

  implementation("org.apache.commons:commons-lang3:3.14.0")
  implementation("org.apache.commons:commons-text:1.10.0")
  implementation("com.pauldijou:jwt-core_2.11:5.0.0")

  // https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk
  implementation("com.amazonaws:aws-java-sdk-s3:$awssdkVersion")
  implementation("com.amazonaws:aws-java-sdk-cloudformation:$awssdkVersion")
  implementation("com.amazonaws:aws-java-sdk-core:$awssdkVersion")
  implementation("com.amazonaws:aws-java-sdk-ec2:$awssdkVersion")
  implementation("com.amazonaws:aws-java-sdk-kms:$awssdkVersion")
  implementation("com.amazonaws:aws-java-sdk-sns:$awssdkVersion")
  implementation("com.amazonaws:aws-java-sdk-sqs:$awssdkVersion")
  implementation("com.amazonaws:aws-java-sdk-sts:$awssdkVersion")
  implementation("com.amazonaws:jmespath-java:$awssdkVersion")

  runtimeOnly("org.glassfish.jaxb:jaxb-runtime")

  testImplementation("junit:junit:4.13.2")
  testImplementation("io.github.http-builder-ng:http-builder-ng-apache:1.0.4")
  testImplementation("org.testcontainers:localstack:1.20.6")
  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("com.google.code.gson:gson:2.11.0")
  testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
  testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "21"
    }
  }
  withType<Task> {
    ktlintTestSourceSetCheck {
      enabled = false
    }
  }
  withType<Task> {
    ktlintMainSourceSetCheck {
      enabled = false
    }
  }
  withType<Task> {
    ktlintKotlinScriptCheck {
      enabled = false
    }
  }
}
