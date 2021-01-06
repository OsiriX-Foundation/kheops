package online.kheops.auth_server.inbox;



import javax.xml.bind.annotation.XmlElement;
import java.util.*;

public class InboxInfoResponse {

    @XmlElement(name = "modalities")
    private SortedSet<String> modalities;
    @XmlElement(name = "number_of_studies")
    private Integer numberOfStudies;
    @XmlElement(name = "number_of_series")
    private Integer numberOfSeries;
    @XmlElement(name = "number_of_instances")
    private Integer numberOfInstances;

    private InboxInfoResponse() { /*empty*/ }

    public InboxInfoResponse(long a, long b, long c, String s) {
        this.numberOfStudies = ((Long) a).intValue();
        this.numberOfSeries = ((Long) b).intValue();
        this.numberOfInstances = ((Long) c).intValue();
        this.modalities = new TreeSet<>();
        this.modalities.addAll(Arrays.asList(s.substring(1, s.length()-1).split(",")));
    }

}
