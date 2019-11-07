package online.kheops.auth_server.event;

import online.kheops.auth_server.entity.Comment;
import online.kheops.auth_server.entity.Mutation;
import online.kheops.auth_server.user.UserResponse;
import online.kheops.auth_server.user.UserResponseBuilder;

import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;
import java.util.HashMap;

public class EventResponse {

    private static class SeriesResponse {
        @XmlElement(name = "UID")
        private String seriesUID;
        @XmlElement(name = "description")
        private String seriesDescription;
    }

    private static class StudyResponse {
        @XmlElement(name = "UID")
        private String studyUID;
        @XmlElement(name = "description")
        private String studyDescription;
    }

    private static class CapabilityResponse {
        @XmlElement(name = "title")
        private String title;
        @XmlElement(name = "id")
        private String id;
    }

    private static class ReportProviderResponse {
        @XmlElement(name = "name")
        private String name;
        @XmlElement(name = "id")
        private String id;
        @XmlElement(name = "is_removed")
        private boolean removed;

    }


    @XmlElement(name = "event_type")
    private String eventType;

    //Comment
    @XmlElement(name = "origin")
    private UserResponse origin;
    @XmlElement(name = "comment")
    private String comment;
    @XmlElement(name = "post_date")
    private LocalDateTime postDate;
    @XmlElement(name = "is_private")
    private Boolean privateComment;
    @XmlElement(name = "target")
    private UserResponse target;

    //Mutation
    @XmlElement(name = "mutation_type")
    private String mutationType;
    @XmlElement(name = "series")
    private SeriesResponse series;
    @XmlElement(name = "study")
    private StudyResponse study;
    @XmlElement(name = "capability")
    private CapabilityResponse capability;
    @XmlElement(name = "report_provider")
    private ReportProviderResponse reportProvider;

    private EventResponse() { /*empty*/ }

    public EventResponse(Comment comment, HashMap<String, Boolean> userMember) {

        eventType = "Comment";
        origin = new UserResponseBuilder()
                .setUser(comment.getUser())
                .setCanAccess(userMember.get(comment.getUser().getKeycloakId()))
                .build();
        this.comment = comment.getComment();
        postDate = comment.getEventTime();
        if (comment.getPrivateTargetUser() != null) {
            privateComment = true;
            target = new UserResponseBuilder()
                    .setUser(comment.getPrivateTargetUser())
                    .setCanAccess(userMember.get(comment.getPrivateTargetUser().getKeycloakId()))
                    .build();
        } else {
            privateComment = false;
        }
    }

    public EventResponse(Mutation mutation, HashMap<String, Boolean> userMember) {

        eventType = "Mutation";

        origin = new UserResponseBuilder()
                .setUser(mutation.getUser())
                .setCanAccess(userMember.get(mutation.getUser().getKeycloakId()))
                .build();
        postDate = mutation.getEventTime();
        mutationType = mutation.getMutationType();

        if (mutationType.equals(Events.MutationType.PROMOTE_ADMIN.toString()) ||
                mutationType.equals(Events.MutationType.DEMOTE_ADMIN.toString()) ||
                mutationType.equals(Events.MutationType.ADD_USER.toString()) ||
                mutationType.equals(Events.MutationType.ADD_ADMIN.toString()) ||
                mutationType.equals(Events.MutationType.REMOVE_USER.toString())) {
            target = new UserResponseBuilder()
                    .setUser(mutation.getToUser())
                    .setCanAccess(userMember.get(mutation.getToUser().getKeycloakId()))
                    .build();
        }
        if (mutationType.equals(Events.MutationType.IMPORT_SERIES.toString()) ||
                mutation.getMutationType().equals(Events.MutationType.REMOVE_SERIES.toString()) ||
                mutation.getMutationType().equals(Events.MutationType.NEW_REPORT.toString())) {
            series = new SeriesResponse();
            study = new StudyResponse();
            series.seriesUID = mutation.getSeries().getSeriesInstanceUID();
            series.seriesDescription = mutation.getSeries().getSeriesDescription();
            study.studyUID = mutation.getStudy().getStudyInstanceUID();
            study.studyDescription = mutation.getStudy().getStudyDescription();
        }
        if (mutationType.equals(Events.MutationType.ADD_FAV.toString()) ||
                mutationType.equals(Events.MutationType.REMOVE_FAV.toString())) {
            if (mutation.getSeries() != null) {
                series = new SeriesResponse();
                series.seriesUID = mutation.getSeries().getSeriesInstanceUID();
                series.seriesDescription = mutation.getSeries().getSeriesDescription();
            }
            study = new StudyResponse();
            study.studyUID = mutation.getStudy().getStudyInstanceUID();
            study.studyDescription = mutation.getStudy().getStudyDescription();
        }
        if (mutationType.equals(Events.MutationType.IMPORT_STUDY.toString()) ||
                mutationType.equals(Events.MutationType.REMOVE_STUDY.toString())) {
            study = new StudyResponse();
            study.studyUID = mutation.getStudy().getStudyInstanceUID();
            study.studyDescription = mutation.getStudy().getStudyDescription();
        }
        mutation.getReportProvider().ifPresent(mutationReportProvider -> {
            reportProvider = new ReportProviderResponse();
            if(mutationReportProvider.isRemoved()) {
                reportProvider.removed = true;
            } else {
                reportProvider.removed = false;
                reportProvider.id = mutationReportProvider.getClientId();
            }

            reportProvider.name = mutationReportProvider.getName();
        });
        mutation.getCapability().ifPresent(mutationCapability -> {
            capability = new CapabilityResponse();
            capability.id = mutationCapability.getId();
            capability.title = mutationCapability.getTitle();
        });
    }
}
