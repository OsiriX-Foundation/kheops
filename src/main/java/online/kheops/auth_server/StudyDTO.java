package online.kheops.auth_server;

import java.util.HashSet;
import java.util.Set;

public class StudyDTO {
    public Set<SeriesDTO> getSeries() {
        return series;
    }

    private Set<SeriesDTO> series = new HashSet<>();

}
