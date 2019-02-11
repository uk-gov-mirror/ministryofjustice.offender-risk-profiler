package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.*;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocg;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocgm;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Pras;
import uk.gov.justice.digital.hmpps.riskprofiler.model.Alert;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SocDecisionTreeServiceTest {

    private static final String OFFENDER_1 = "AB1234A";
    private SocDecisionTreeService service;

    @Mock
    private NomisService nomisService;
    @Mock
    private PrasRepository prasRepo;
    @Mock
    private OcgRepository ocgRepo;
    @Mock
    private OcgmRepository ocgmRepo;
    @Mock
    private PathfinderRepository pathfinderRepo;
    @Mock
    private ViperRepository viperRepo;

    @Before
    public void setup() {
        var factory = new DataRepositoryFactory(ocgmRepo, ocgRepo, pathfinderRepo, prasRepo, viperRepo);
        service = new SocDecisionTreeService(factory, nomisService);
    }

    @Test
    public void testOnPrasFile() {
        when(prasRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(Pras.builder().nomisId(OFFENDER_1).build()));
        var socProfile = service.getSocData(OFFENDER_1);

        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.isTransferToSecurity()).isTrue();
    }

    @Test
    public void testNotOnPrasFileAndBandNotInList() {
        var xfo = Alert.builder().active(true).alertCode("XFO").dateCreated(LocalDate.now().minusMonths(11)).build();
        var xd = Alert.builder().active(false).alertCode("XD").dateExpires(LocalDate.now().minusYears(2)).build();

        when(prasRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        when(ocgmRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(Ocgm.builder().nomisId(OFFENDER_1).ocgId("123").build()));
        when(ocgRepo.getByKey(eq("123"))).thenReturn(Optional.of(Ocg.builder().ocgmBand("5c").build()));
        when(nomisService.getSocListAlertsForOffender(OFFENDER_1)).thenReturn(List.of(xfo, xd));

        var socProfile = service.getSocData(OFFENDER_1);
        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.isTransferToSecurity()).isFalse();
    }

    @Test
    public void testNotOnPrasFileAndBandInList() {
        when(prasRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        when(ocgmRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(Ocgm.builder().nomisId(OFFENDER_1).ocgId("1234").build()));
        when(ocgRepo.getByKey(eq("1234"))).thenReturn(Optional.of(Ocg.builder().ocgmBand("2a").build()));

        var socProfile = service.getSocData(OFFENDER_1);
        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.isTransferToSecurity()).isFalse();
    }


    @Test
    public void testNotOnPrasFileAndBandInListAndPrincipleStanding() {
        when(prasRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        when(ocgmRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(Ocgm.builder().nomisId(OFFENDER_1)
                .standingWithinOcg("Principal Subject")
                .ocgId("12345").build()));
        when(ocgRepo.getByKey(eq("12345"))).thenReturn(Optional.of(Ocg.builder().ocgmBand("2a").build()));

        var socProfile = service.getSocData(OFFENDER_1);
        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.isTransferToSecurity()).isTrue();
    }

    @Test
    public void testNotOnPrasFileAndNotInBandInListAndPrincipleStanding() {
        when(prasRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        when(ocgmRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(Ocgm.builder().nomisId(OFFENDER_1)
                .standingWithinOcg("Principal Subject")
                .ocgId("123456").build()));
        when(ocgRepo.getByKey(eq("123456"))).thenReturn(Optional.of(Ocg.builder().ocgmBand("4a").build()));

        var socProfile = service.getSocData(OFFENDER_1);
        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.isTransferToSecurity()).isTrue();
    }

    @Test
    public void testNotOnPrasFileAndBandNotInListWithOldAlerts() {
        var now = LocalDate.now();
        var xfo = Alert.builder().active(true).alertCode("XFO").dateCreated(now.minusMonths(13)).build();
        var xd = Alert.builder().active(false).alertCode("XD").dateExpires(now.minusYears(2)).dateExpires(now.minusMonths(16)).expired(true).build();

        when(prasRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        when(ocgmRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(Ocgm.builder().nomisId(OFFENDER_1).ocgId("123").build()));
        when(ocgRepo.getByKey(eq("123"))).thenReturn(Optional.of(Ocg.builder().ocgmBand("5c").build()));
        when(nomisService.getSocListAlertsForOffender(OFFENDER_1)).thenReturn(List.of(xfo, xd));

        var socProfile = service.getSocData(OFFENDER_1);
        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.isTransferToSecurity()).isFalse();
    }

    @Test
    public void testNotOnPrasFileAndNoOcgmWithActiveAlerts() {
        var now = LocalDate.now();
        var xfo = Alert.builder().active(true).alertCode("XFO").dateCreated(now.minusMonths(11)).build();
        var xd = Alert.builder().active(false).alertCode("XD").dateExpires(now.minusYears(2)).build();

        when(prasRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        when(ocgmRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        when(nomisService.getSocListAlertsForOffender(OFFENDER_1)).thenReturn(List.of(xfo, xd));

        var socProfile = service.getSocData(OFFENDER_1);
        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.isTransferToSecurity()).isFalse();
    }

    @Test
    public void testNotOnPrasFileAndNoOcgmWithOldAlerts() {
        var now = LocalDate.now();
        var xfo = Alert.builder().active(true).alertCode("XFO").dateCreated(now.minusMonths(13)).build();
        var xd = Alert.builder().active(false).alertCode("XD").dateExpires(now.minusYears(2)).dateExpires(now.minusMonths(16)).expired(true).build();

        when(prasRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        when(ocgmRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        when(nomisService.getSocListAlertsForOffender(OFFENDER_1)).thenReturn(List.of(xfo, xd));

        var socProfile = service.getSocData(OFFENDER_1);
        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.isTransferToSecurity()).isFalse();
    }

    @Test
    public void testNotOnPrasFileAndHasOcgmNotNoOcgWithOldAlerts() {
        var now = LocalDate.now();
        var xfo = Alert.builder().active(true).alertCode("XFO").dateCreated(now.minusMonths(13)).build();
        var xd = Alert.builder().active(false).alertCode("XD").dateExpires(now.minusYears(2)).dateExpires(now.minusMonths(16)).expired(true).build();

        when(prasRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        when(ocgmRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(Ocgm.builder().nomisId(OFFENDER_1).ocgId("123").build()));
        when(ocgRepo.getByKey(eq("123"))).thenReturn(Optional.empty());
        when(nomisService.getSocListAlertsForOffender(OFFENDER_1)).thenReturn(List.of(xfo, xd));

        var socProfile = service.getSocData(OFFENDER_1);
        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.isTransferToSecurity()).isFalse();
    }
}