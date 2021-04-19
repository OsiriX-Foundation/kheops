package online.kheops.auth_server.util;

import java.util.List;

public class PairListXTotalCount<T> {
    private long XTotalCount;
    private List<T> attributesList;

    public PairListXTotalCount(long XTotalCount, List<T> attributesList) {
        this.attributesList = attributesList;
        this.XTotalCount = XTotalCount;
    }

    public long getXTotalCount() {
        return XTotalCount;
    }

    public List<T> getAttributesList() {
        return attributesList;
    }
}

