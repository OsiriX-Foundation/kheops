package online.kheops.auth_server.event;

import online.kheops.auth_server.entity.Comment;
import online.kheops.auth_server.entity.Mutation;

import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;

public class EventResponses {

    private EventResponses() { throw new IllegalStateException("Utility class"); }

    public static class EventResponse {
        @XmlElement(name = "event_type")
        public String eventType;

        @XmlElement(name = "origin_name")
        public String originName;
        @XmlElement(name = "comment")
        public String comment;
        @XmlElement(name = "post_date")
        public LocalDateTime postDate;
        @XmlElement(name = "is_private")
        public Boolean privateComment;
        @XmlElement(name = "target_name")
        public String targetName;

        @XmlElement(name = "mutation_type")
        public String mutationType;
        @XmlElement(name = "series")
        public String series;
        @XmlElement(name = "study")
        public String study;
    }

    public static EventResponses.EventResponse commentToEventResponse(Comment comment) {
        final EventResponse eventResponse = new EventResponse();

        eventResponse.eventType = "Comment";
        eventResponse.originName = comment.getUser().getEmail();
        eventResponse.comment = comment.getComment();
        eventResponse.postDate = comment.getEventTime();
        if(comment.getPrivateTargetUser() != null) {
            eventResponse.privateComment = true;
            eventResponse.targetName = comment.getPrivateTargetUser().getEmail();
        } else {
            eventResponse.privateComment = false;
        }

        return eventResponse;
    }

    public static EventResponses.EventResponse mutationToEventResponse(Mutation mutation) {
        final EventResponse eventResponse = new EventResponse();

        eventResponse.eventType = "Mutation";

        eventResponse.originName = mutation.getUser().getEmail();
        eventResponse.postDate = mutation.getEventTime();
        eventResponse.mutationType = mutation.getMutationType();
        if (mutation.getMutationType().compareTo(Events.MutationType.PROMOTE_ADMIN.toString()) == 0 ||
                mutation.getMutationType().compareTo(Events.MutationType.DEMOTE_ADMIN.toString()) == 0 ||
                mutation.getMutationType().compareTo(Events.MutationType.ADD_USER.toString()) == 0 ||
                mutation.getMutationType().compareTo(Events.MutationType.ADD_ADMIN.toString()) == 0 ||
                mutation.getMutationType().compareTo(Events.MutationType.REMOVE_USER.toString()) == 0 ) {
            eventResponse.targetName = mutation.getToUser().getEmail();
        }
        if (mutation.getMutationType().compareTo(Events.MutationType.IMPORT_SERIES.toString()) == 0 ||
                mutation.getMutationType().compareTo(Events.MutationType.REMOVE_SERIES.toString()) == 0 ) {
            eventResponse.series = mutation.getSeries().getSeriesInstanceUID();
            eventResponse.study = mutation.getSeries().getStudy().getStudyInstanceUID();
        }
        if (mutation.getMutationType().compareTo(Events.MutationType.IMPORT_STUDY.toString()) == 0 ||
                mutation.getMutationType().compareTo(Events.MutationType.REMOVE_STUDY.toString()) == 0 ) {
            eventResponse.study = mutation.getStudy().getStudyInstanceUID();
        }

        return eventResponse;
    }
}
