package online.kheops.auth_server;

import online.kheops.auth_server.entity.Series;

public class SeriesDTO {

    public SeriesDTO() {}

    public SeriesDTO(Series series) {
        this.modality = series.getModality();
    }

    private String modality;

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }
}
