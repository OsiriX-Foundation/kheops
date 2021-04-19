package online.kheops.auth_server.inbox;


import org.jooq.Record;

import javax.xml.bind.annotation.XmlElement;
import java.math.BigDecimal;

import static online.kheops.auth_server.util.JooqConstances.*;

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
        this.numberOfStudies = (Integer) r.getValue(NUMBER_OF_STUDIES);
        this.numberOfSeries = (Integer) r.getValue(NUMBER_OF_SERIES);
        try {
            this.numberOfInstances = ((BigDecimal) r.getValue(NUMBER_OF_INSTANCES)).intValue();
        } catch(NullPointerException e) {
            this.numberOfInstances = 0;
        }

        if(r.getValue(MODALITIES) != null) {
            this.modalities = r.getValue(MODALITIES).toString().split(",");
        } else {
            this.modalities = new String[0];
        }
    }

}
