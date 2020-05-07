package online.kheops.auth_server.entity;


import javax.persistence.*;

@SuppressWarnings({"WeakerAccess", "unused"})
@Entity
@Table(name = "event_series")
public class EventSeries {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @ManyToOne
    @JoinColumn (name = "event_fk", nullable=false, insertable = true, updatable = false)
    private Event event;

    @ManyToOne
    @JoinColumn (name = "series_fk", nullable=false, insertable = true, updatable = false)
    private Series series;

    public EventSeries() {}

    public EventSeries(Event event, Series series) {
        this.event = event;
        this.series = series;
        series.addEventSeries(this);
        event.addEventSeries(this);
    }

    public Series getSeries() {
        return series;
    }
}
