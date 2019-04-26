package online.kheops.auth_server.event;

import online.kheops.auth_server.entity.Comment;
import online.kheops.auth_server.entity.Mutation;

import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;

public class EventResponse {

    private Response response;

    public static class SeriesResponse {
        @XmlElement(name = "UID")
        public String seriesUID;

        @XmlElement(name = "description")
        public String seriesDescription;
    }

    public static class StudyResponse {
        @XmlElement(name = "UID")
        public String studyUID;

        @XmlElement(name = "description")
        public String studyDescription;
    }

    public static class Response {
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
        public SeriesResponse series;
        @XmlElement(name = "study")
        public StudyResponse study;
        @XmlElement(name = "capability")
        public CapabilityResponse capability;
    }

    public static class CapabilityResponse {
        @XmlElement(name = "title")
        public String title;
        @XmlElement(name = "id")
        public String id;
    }


    public EventResponse(Comment comment) {
        response = new Response();

        response.eventType = "Comment";
        response.originName = comment.getUser().getEmail();
        response.comment = comment.getComment();
        response.postDate = comment.getEventTime();
        if (comment.getPrivateTargetUser() != null) {
            response.privateComment = true;
            response.targetName = comment.getPrivateTargetUser().getEmail();
        } else {
            response.privateComment = false;
        }
    }

    public EventResponse(Mutation mutation) {
        response = new Response();

        response.eventType = "Mutation";

        response.originName = mutation.getUser().getEmail();
        response.postDate = mutation.getEventTime();
        response.mutationType = mutation.getMutationType();
        if (mutation.getMutationType().equals(Events.MutationType.PROMOTE_ADMIN.toString()) ||
                mutation.getMutationType().equals(Events.MutationType.DEMOTE_ADMIN.toString()) ||
                mutation.getMutationType().equals(Events.MutationType.ADD_USER.toString()) ||
                mutation.getMutationType().equals(Events.MutationType.ADD_ADMIN.toString()) ||
                mutation.getMutationType().equals(Events.MutationType.REMOVE_USER.toString())) {
            response.targetName = mutation.getToUser().getEmail();
        }
        if (mutation.getMutationType().equals(Events.MutationType.IMPORT_SERIES.toString()) ||
                mutation.getMutationType().equals(Events.MutationType.REMOVE_SERIES.toString())) {
            response.series = new SeriesResponse();
            response.study = new StudyResponse();
            response.series.seriesUID = mutation.getSeries().getSeriesInstanceUID();
            response.series.seriesDescription = mutation.getSeries().getSeriesDescription();
            response.study.studyUID = mutation.getStudy().getStudyInstanceUID();
            response.study.studyDescription = mutation.getStudy().getStudyDescription();
        }
        if (mutation.getMutationType().equals(Events.MutationType.ADD_FAV.toString()) ||
                mutation.getMutationType().equals(Events.MutationType.REMOVE_FAV.toString())) {
            if (mutation.getSeries() != null) {
                response.series = new SeriesResponse();
                response.series.seriesUID = mutation.getSeries().getSeriesInstanceUID();
                response.series.seriesDescription = mutation.getSeries().getSeriesDescription();
            }
            response.study = new StudyResponse();
            response.study.studyUID = mutation.getStudy().getStudyInstanceUID();
            response.study.studyDescription = mutation.getStudy().getStudyDescription();
        }
        if (mutation.getMutationType().equals(Events.MutationType.IMPORT_STUDY.toString()) ||
                mutation.getMutationType().equals(Events.MutationType.REMOVE_STUDY.toString())) {
            response.study = new StudyResponse();
            response.study.studyUID = mutation.getStudy().getStudyInstanceUID();
            response.study.studyDescription = mutation.getStudy().getStudyDescription();
        }
        if (mutation.getCapability().isPresent()) {
            final CapabilityResponse capabilityResponse = new CapabilityResponse();
            capabilityResponse.id = mutation.getCapability().get().getId();
            capabilityResponse.title = mutation.getCapability().get().getTitle();
            response.capability = capabilityResponse;
        }
    }

    public Response getResponse() {
        return response;
    }
}
