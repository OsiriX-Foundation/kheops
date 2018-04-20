package online.kheops.auth_server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class StudyDTO {
    public List<SeriesDTO> getSeries() {
        return series;
    }

    public String[] getModalities() {
        String[] modalities = new String[getSeries().size()];

        for (int i = 0; i < getSeries().size(); i++) {
            modalities[i] = getSeries().get(i).getModality();
        }

        return modalities;
    }

    private List<SeriesDTO> series = new ArrayList<>();

}
