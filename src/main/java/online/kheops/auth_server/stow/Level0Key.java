package online.kheops.auth_server.stow;


import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.webhook.Source;

import java.util.Objects;


public class Level0Key {

    private Study study;
    private Source source;

    Level0Key(Study study, Source source) {
        this.study = study;
        this.source = source;
    }

    public Study getStudy() { return study; }

    public Source getSource() { return source; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Level0Key level0Key = (Level0Key) o;
        return level0Key.study.equals(study) && level0Key.source.equals(source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(study, source);
    }
}
