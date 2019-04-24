package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocg;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.FileType.OCG;

@Repository
@Slf4j
public class OcgRepository implements DataRepository<Ocg> {

    private final ImportedFile<Ocg> data = new ImportedFile<>();

    public void process(List<List<String>> csvData, final String filename, final LocalDateTime timestamp) {

        data.setFileTimestamp(timestamp);
        data.setFileName(filename);
        data.setFileType(OCG);
        data.reset();

        csvData.stream().filter(p -> data.getIndex().getAndIncrement() > 0)
                .forEach(p -> {
                    try {
                        final var key = p.get(Ocg.OCG_ID_POSITION);
                        if (StringUtils.isNotBlank(key)) {

                            if (data.getDataSet().get(key) != null) {
                                log.warn("Duplicate key found in line {} for Key {}", data.getIndex().get(), key);
                                data.getLinesDup().incrementAndGet();
                            } else {
                                var ocgLine = Ocg.builder()
                                        .ocgId(key)
                                        .ocgmBand(StringUtils.trimToNull(p.get(Ocg.OCGM_BAND_POSITION)))
                                        .build();

                                data.getDataSet().put(key, ocgLine);
                                data.getLinesProcessed().incrementAndGet();
                            }
                        } else {
                            log.warn("Missing Key in line {} key [{}]", data.getIndex().get(), key);
                            data.getLinesInvalid().incrementAndGet();
                        }
                    } catch (Exception e) {
                        log.warn("Error in Line {} data [{}]", data.getIndex(), p);
                        data.getLinesError().incrementAndGet();
                    }
                });
        log.info("Lines total {}, processed {}, dups {}, invalid {}, errors {}", data.getIndex().get(),
                data.getLinesProcessed().get(), data.getLinesDup().get(), data.getLinesInvalid().get(), data.getLinesError().get());

    }

    public LocalDateTime getFileTimestamp() {
        return data.getFileTimestamp();
    }

    public ImportedFile<Ocg> getData() {
        return data;
    }

}
