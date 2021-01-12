package online.kheops.auth_server.study;

import online.kheops.auth_server.entity.Study;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

public class StudyResponseDICOM {

    Study study;
    Long nbSeries;
    Long nbInstances;
    String modalities;
    Long nbFavorite;
    Long nbComment;

    public StudyResponseDICOM(Study study, Long nbSeries, Long nbInstances, String modalities,Long nbFavorite, Long nbComment) {
        this.study = study;
        this.nbSeries = nbSeries;
        this.nbInstances = nbInstances;
        this.nbComment = nbComment;
        this.nbFavorite = nbFavorite;
        this.modalities = modalities;

        SortedSet<String >mod = new TreeSet<>();
        mod.addAll(Arrays.asList(modalities.substring(1, modalities.length() - 1).split(",")));
        mod.remove("NULL");
        this.modalities = mod.toString();
    }

    @Override
    public String toString() {
        return "com:"+nbComment+" series:"+nbSeries+" instances:"+nbInstances+" fav:"+nbFavorite+" mod:"+modalities+" study:"+study.toString();
    }
}
