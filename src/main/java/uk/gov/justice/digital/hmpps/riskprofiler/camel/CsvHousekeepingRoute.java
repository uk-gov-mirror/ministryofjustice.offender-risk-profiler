package uk.gov.justice.digital.hmpps.riskprofiler.camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.hmpps.riskprofiler.services.FileService;

@Component
public class CsvHousekeepingRoute extends RouteBuilder {

    private final FileService fileService;

    public CsvHousekeepingRoute(FileService fileService) {
        this.fileService = fileService;
    }

    @Override
    public void configure() {

        from("timer://data-deletion-schedule?fixedRate=true&period={{data.deletion.period}}")
                .bean(fileService, "deleteHistoricalFiles('{{s3.path.ocg}}')")
                .bean(fileService, "deleteHistoricalFiles('{{s3.path.ocgm}}')")
                .bean(fileService, "deleteHistoricalFiles('{{s3.path.pras}}')")
                .bean(fileService, "deleteHistoricalFiles('{{s3.path.viper}}')");
    }
}
