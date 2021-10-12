package online.kheops.auth_server.entity;

import javax.persistence.*;

import static online.kheops.auth_server.util.JPANamedQueryConstants.*;

@NamedQueries({
        @NamedQuery(name = "Event.deleteAllEventSeriesBySeries",
                query = "DELETE FROM EventSeries es WHERE es.series = :"+SERIES),
})

@Entity
@Table(name = "event_series")
public class EventSeries {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @ManyToOne
    @JoinColumn (name = "event_fk", nullable=false, insertable = true, updatable = false)
    private Event event;

    @ManyToOne
    @JoinColumn (name = "series_fk", nullable=false, insertable = true, updatable = false)
    private Series series;

    public EventSeries() {}

    public EventSeries(Album album, Series series) {
        this.event = event;
        this.series = series;
    }
}
