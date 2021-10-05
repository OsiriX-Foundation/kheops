package online.kheops.auth_server.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(EventSeries.class)
public abstract class EventSeries_ {

    public static volatile SingularAttribute<EventSeries, Event> event;
    public static volatile SingularAttribute<EventSeries, Series> series;
    public static volatile SingularAttribute<EventSeries, Long> pk;

    public static final String EVENT = "event";
    public static final String SERIES = "series";
    public static final String PK = "pk";

}

