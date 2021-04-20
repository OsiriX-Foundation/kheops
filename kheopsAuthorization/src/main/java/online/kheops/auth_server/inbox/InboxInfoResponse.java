package online.kheops.auth_server.inbox;


import org.jooq.Record;

import javax.xml.bind.annotation.XmlElement;
import java.math.BigDecimal;

public class InboxInfoResponse {

    @XmlElement(name = "modalities")
    private String[] modalities;
    @XmlElement(name = "number_of_studies")
    private Integer numberOfStudies;
    @XmlElement(name = "number_of_series")
    private Integer numberOfSeries;
    @XmlElement(name = "number_of_instances")
    private Integer numberOfInstances;

    private InboxInfoResponse() { /*empty*/ }

    protected InboxInfoResponse(Record r) {
        this.numberOfStudies = (Integer) r.getValue("number_of_studies");
        this.numberOfSeries = (Integer) r.getValue("number_of_series");
        try {
            this.numberOfInstances = ((BigDecimal) r.getValue("number_of_instances")).intValue();
        } catch(NullPointerException e) {
            this.numberOfInstances = 0;
        }

        if(r.getValue("modalities") != null) {
            this.modalities = r.getValue("modalities").toString().split(",");
        } else {
            this.modalities = new String[0];
        }
    }

}
