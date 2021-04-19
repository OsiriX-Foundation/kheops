package online.kheops.auth_server.webhook.delayed_webhook;


import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.webhook.Source;

import java.util.Objects;


public class StudySourceKey {

    private long studyPk;
    private Source source;

    StudySourceKey(long studyPk, Source source) {
        this.studyPk = studyPk;
        this.source = source;
    }

    public long getStudyPk() { return studyPk; }

    public Source getSource() { return source; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudySourceKey studySourceKey = (StudySourceKey) o;
        return studySourceKey.studyPk == studyPk && studySourceKey.source.equals(source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studyPk, source);
    }
}
