package online.kheops.auth_server.study;

import org.dcm4che3.data.Attributes;
import java.util.List;

public class PairStudiesListStudiesTotalCount {
    private Integer studiesTotalCount;
    private List<Attributes> attributesList;

    public PairStudiesListStudiesTotalCount(Integer studiesTotalCount, List<Attributes> attributesList) {
        this.attributesList = attributesList;
        this.studiesTotalCount = studiesTotalCount;
    }

    public Integer getStudiesTotalCount() {
        return studiesTotalCount;
    }

    public List<Attributes> getAttributesList() {
        return attributesList;
    }
}
