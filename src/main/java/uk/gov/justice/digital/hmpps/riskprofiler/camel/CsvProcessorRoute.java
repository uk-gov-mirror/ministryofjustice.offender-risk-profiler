package uk.gov.justice.digital.hmpps.riskprofiler.camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static uk.gov.justice.digital.hmpps.riskprofiler.camel.CsvProcessor.PROCESS_CSV;

@Component
@ConditionalOnProperty(name = "file.process.type", havingValue = "file")
public class CsvProcessorRoute extends RouteBuilder {

    private final CsvProcessor csvProcessor;

    public CsvProcessorRoute(CsvProcessor csvProcessor) {
        this.csvProcessor = csvProcessor;
    }

    @Override
    public void configure() {

        from(PROCESS_CSV)
                .unmarshal().csv()
                .bean(csvProcessor, "doHandleFileCsvData");

    }
}
