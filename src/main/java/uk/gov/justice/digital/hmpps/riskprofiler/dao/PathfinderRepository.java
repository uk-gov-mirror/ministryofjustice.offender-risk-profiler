package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.PathFinder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.FileType.PATHFINDER;

@Repository
@Slf4j
public class PathfinderRepository implements DataRepository<PathFinder>{

    private final ImportedFile<PathFinder> data = new ImportedFile<>();

    public void process(List<List<String>> csvData, final String filename, final LocalDateTime timestamp) {
        data.setFileTimestamp(timestamp);
        data.setFileName(filename);
        data.setFileType(PATHFINDER);
        data.reset();

        csvData.stream().filter(p -> data.getIndex().getAndIncrement() > 0)
                .forEach(p -> {
                    try {
                        final var key = p.get(PathFinder.NOMIS_ID_POSITION);
                        if (StringUtils.isNotBlank(key)) {

                            if (!NOMS_ID_REGEX.matcher(key).matches()) {
                                log.warn("Invalid Key in line {} for Key {}", data.getIndex().get(), key);
                                data.getLinesInvalid().incrementAndGet();
                            } else {
                                if (data.getDataSet().get(key) != null) {
                                    log.warn("Duplicate key found in line {} for Key {}", data.getIndex().get(), key);
                                    data.getLinesDup().incrementAndGet();
                                } else {

                                    var banding = p.get(PathFinder.PATH_FINDER_BANDING_POSITION);
                                    if (StringUtils.isBlank(banding)) {
                                        log.warn("No Banding set in line {} for Key {}", data.getIndex().get(), key);
                                        data.getLinesInvalid().incrementAndGet();
                                    } else {
                                        var ocgLine = PathFinder.builder()
                                                .nomisId(key)
                                                .pathFinderBanding(StringUtils.trimToNull(banding))
                                                .build();

                                        data.getDataSet().put(key, ocgLine);
                                        data.getLinesProcessed().incrementAndGet();
                                    }
                                }
                            }
                        } else {
                            log.warn("Missing Key in line {}", data.getIndex().get(), key);
                            data.getLinesInvalid().incrementAndGet();
                        }
                    } catch (Exception e) {
                        log.warn("Error in Line {}", data.getIndex(), p);
                        data.getLinesError().incrementAndGet();
                    }
                });

        log.info("Lines total {}, processed {}, dups {}, invalid {}, errors {}", data.getIndex().get(),
                data.getLinesProcessed().get(), data.getLinesDup().get(), data.getLinesInvalid().get(), data.getLinesError().get());
    }

    public LocalDateTime getFileTimestamp() {
        return data.getFileTimestamp();
    }

    public ImportedFile<PathFinder> getData() {
        return data;
    }

    public Optional<PathFinder> getByKey(String key) {
        return Optional.ofNullable(data.getDataSet().get(key));
    }

}
