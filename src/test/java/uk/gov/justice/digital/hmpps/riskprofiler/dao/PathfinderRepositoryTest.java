package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import org.junit.Test;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.PathFinder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PathfinderRepositoryTest {

    @Test
    public void testPathFinder() {
        List<String> row1 = Arrays.asList("Surname", "Name", "071080", "NomisId", "Status", "status", "Band 1", "est");
        List<String> row2 = Arrays.asList("Surname", "Name", "071080", "A1234AA", "Status", "status", "Band 2", "est");
        List<List<String>> pathFinderList = Arrays.asList(row1, row2);
        DataRepository<PathFinder> repository = new PathfinderRepository();
        repository.process(pathFinderList, "Pathfinder-20190204163820000.csv", LocalDateTime.now());


        Optional<PathFinder> isThere = repository.getByKey("NomisId");
        assertTrue(isThere.isEmpty());

        isThere = repository.getByKey("A1234AA");
        assertTrue(isThere.isPresent());
        var obj = isThere.get();
        assertEquals(obj.getKey(), "A1234AA");
        assertEquals(obj.getPathFinderBanding(), "Band 2");

        assertTrue(repository.getByKey("No there").isEmpty());
    }

}