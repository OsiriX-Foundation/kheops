package online.kheops.auth_server.util;

import java.util.List;

public class PairListXTotalCount<T> {
    private long xTotalCount;
    private List<T> attributesList;

    public PairListXTotalCount(long xTotalCount, List<T> attributesList) {
        this.attributesList = attributesList;
        this.xTotalCount = xTotalCount;
    }

    public long getXTotalCount() {
        return xTotalCount;
    }

    public List<T> getAttributesList() {
        return attributesList;
    }
}

