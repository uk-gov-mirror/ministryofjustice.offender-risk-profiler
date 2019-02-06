package uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@EqualsAndHashCode(of = { "nomisId"})
@ToString
public class Pras implements RiskDataSet {
    public static int NOMIS_ID_POSITION = 11;
    private String nomisId;

    public String getKey() {
        return nomisId;
    }

    public int getKeyPosition() {
        return NOMIS_ID_POSITION;
    }

    @Override
    public FileType getFileType() {
        return FileType.PRAS;
    }
}
