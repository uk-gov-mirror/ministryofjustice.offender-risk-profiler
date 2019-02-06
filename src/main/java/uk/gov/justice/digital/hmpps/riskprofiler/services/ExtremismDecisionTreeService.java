package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.PathfinderRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.PathFinder;
import uk.gov.justice.digital.hmpps.riskprofiler.model.ExtremismProfile;
import uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfile;

import javax.validation.constraints.NotNull;

@Service
public class ExtremismDecisionTreeService {

    private final DataRepository<PathFinder> repository;

    public ExtremismDecisionTreeService(PathfinderRepository repository) {
        this.repository = repository;
    }

    @PreAuthorize("hasRole('RISK_PROFILER')")
    public ExtremismProfile getExtremismProfile(@NotNull final String nomsId, Boolean previousOffences) {
        var pathfinderData = repository.getByKey(nomsId);

        var extremism = ExtremismProfile.extremismBuilder()
                .nomsId(nomsId)
                .provisionalCategorisation(RiskProfile.DEFAULT_CAT);
        // etc

        return extremism.build();

    }
}
