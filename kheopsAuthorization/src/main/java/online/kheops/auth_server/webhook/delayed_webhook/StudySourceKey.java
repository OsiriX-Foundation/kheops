package online.kheops.auth_server.webhook.delayed_webhook;


import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.webhook.Source;

import java.util.Objects;


public class StudySourceKey {

    private Study study;
    private Source source;

    StudySourceKey(Study study, Source source) {
        this.study = study;
        this.source = source;
    }

    public Study getStudy() { return study; }

    public Source getSource() { return source; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudySourceKey studySourceKey = (StudySourceKey) o;
        return studySourceKey.study.equals(study) && studySourceKey.source.equals(source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(study, source);
    }
}
