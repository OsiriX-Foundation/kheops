package online.kheops.auth_server.entity;

import org.dcm4che3.data.Attributes;
import java.util.List;

public class Pair {
    private Integer studiesTotalCount;
    private List<Attributes> attributesList;

    Pair(Integer studiesTotalCount, List<Attributes> attributesList) {
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
