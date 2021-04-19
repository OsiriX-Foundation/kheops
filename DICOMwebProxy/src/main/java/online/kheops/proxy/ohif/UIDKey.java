package online.kheops.proxy.ohif;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;

import javax.xml.bind.annotation.XmlTransient;
import java.util.Comparator;
import java.util.Objects;

@XmlTransient
final class UIDKey implements Comparable<UIDKey> {

    private final String uid;
    private final int number;

    private UIDKey(final String uid, final int number) {
        this.uid = uid;
        this.number = number;
    }

    static UIDKey fromInstance(final Attributes attributes) {
        return new UIDKey(Objects.requireNonNull(attributes.getString(Tag.SOPInstanceUID)), attributes.getInt(Tag.InstanceNumber, 0));
    }

    static UIDKey fromSeries(final Attributes attributes) {
        return new UIDKey(Objects.requireNonNull(attributes.getString(Tag.SeriesInstanceUID)), attributes.getInt(Tag.SeriesNumber, 0));
    }

    static UIDKey fromStudy(final Attributes attributes) {
        return new UIDKey(Objects.requireNonNull(attributes.getString(Tag.StudyInstanceUID)), 0);
    }

    static Comparator<UIDKey> getFirstUIDComparator(final String firstUID) {
        return (uidKey1, uidKey2) -> {
            if (uidKey1.uid.equals(firstUID) && uidKey2.uid.equals(firstUID)) {
                return uidKey1.compareTo(uidKey2);
            } else if (uidKey1.uid.equals(firstUID)) {
                return -1;
            } else if (uidKey2.uid.equals(firstUID)) {
                return 1;
            } else {
                return uidKey1.compareTo(uidKey2);
            }
        };
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof UIDKey)) {
            return false;
        }
        UIDKey key = (UIDKey)o;
        return key.number == number && key.uid.equals(uid);
    }

    @Override
    public int hashCode() {
        return 31 * uid.hashCode() + Integer.hashCode(number);
    }

    @Override
    public int compareTo(final UIDKey uidKey) {
        final int comparison = Integer.compare(number, uidKey.number);
        if (comparison != 0) {
            return comparison;
        } else {
            return uid.compareTo(uidKey.uid);
        }
    }
}
