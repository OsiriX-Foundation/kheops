package online.kheops.auth_server.event;

import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.report_provider.ReportProviderResponse;
import online.kheops.auth_server.user.UserResponse;
import online.kheops.auth_server.user.UserResponseBuilder;

import javax.persistence.EntityManager;
import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventResponse {

    private static class SeriesResponse {
        @XmlElement(name = "UID")
        private String seriesUID;
        @XmlElement(name = "description")
        private String seriesDescription;
        @XmlElement(name = "is_present_in_album")
        private Boolean isPresentInAlbum;
    }

    private static class StudyResponse {
        @XmlElement(name = "UID")
        private String studyUID;
        @XmlElement(name = "description")
        private String studyDescription;
        @XmlElement(name = "series")
        private List<SeriesResponse> seriesResponses;
    }


    @XmlElement(name = "event_type")
    private String eventType;

    @XmlElement(name = "source")
    private UserResponse source;

    //Comment
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
    @XmlElement(name = "study")
    private StudyResponse study;
    @XmlElement(name = "report_provider")
    private ReportProviderResponse reportProvider;


    private EventResponse() { /*empty*/ }

    public EventResponse(Event event, Map<String, Boolean> userMember, EntityManager em) {
        if (event instanceof Comment) {
            this.commentEventResponse((Comment)event, userMember);
        } else if (event instanceof Mutation) {
            this.mutationEventResponse((Mutation) event, userMember, em);
        }
    }

    private void commentEventResponse(Comment comment, Map<String, Boolean> userMember) {

        eventType = "Comment";
        final UserResponseBuilder userResponseBuilder = new UserResponseBuilder()
                .setUser(comment.getUser())
                .setCanAccess(userMember.containsKey(comment.getUser().getSub()));
        if (comment.getAlbum() != null && userMember.containsKey(comment.getUser().getSub())) {
            userResponseBuilder.isAdmin(userMember.get(comment.getUser().getSub()));
        }
        source = userResponseBuilder.build();
        this.comment = comment.getComment();
        postDate = comment.getEventTime();
        if (comment.getPrivateTargetUser() != null) {
            privateComment = true;
            final UserResponseBuilder targetResponseBuilder = new UserResponseBuilder()
                    .setUser(comment.getPrivateTargetUser())
                    .setCanAccess(userMember.containsKey(comment.getPrivateTargetUser().getSub()));
            if (comment.getAlbum() != null && userMember.containsKey(comment.getPrivateTargetUser().getSub())) {
                targetResponseBuilder.isAdmin(userMember.get(comment.getPrivateTargetUser().getSub()));
            }
            target = targetResponseBuilder.build();
        } else {
            privateComment = false;
        }
    }

    private void mutationEventResponse(Mutation mutation, Map<String, Boolean> userMember, EntityManager em) {

        eventType = "Mutation";

        final UserResponseBuilder userResponseBuilder = new UserResponseBuilder()
                .setUser(mutation.getUser())
                .setCanAccess(userMember.containsKey(mutation.getUser().getSub()));
        if (userMember.containsKey(mutation.getUser().getSub())) {
            userResponseBuilder.isAdmin(userMember.get(mutation.getUser().getSub()));
        }
        source = userResponseBuilder.build();
        postDate = mutation.getEventTime();
        mutationType = mutation.getMutationType().toString();
        final MutationType mutationType_ = mutation.getMutationType();

        if (mutationType_.equals(MutationType.PROMOTE_ADMIN) ||
                mutationType_.equals(MutationType.DEMOTE_ADMIN) ||
                mutationType_.equals(MutationType.ADD_USER) ||
                mutationType_.equals(MutationType.ADD_ADMIN) ||
                mutationType_.equals(MutationType.REMOVE_USER)) {
            final UserResponseBuilder targetUserResponseBuilder = new UserResponseBuilder()
                    .setUser(mutation.getToUser())
                    .setCanAccess(userMember.containsKey(mutation.getToUser().getSub()));
            if (userMember.containsKey(mutation.getToUser().getSub())) {
                targetUserResponseBuilder.isAdmin(userMember.get(mutation.getToUser().getSub()));
            }
            target = targetUserResponseBuilder.build();

        }
        if (mutationType_.equals(MutationType.IMPORT_SERIES) ||
                mutationType_.equals(MutationType.REMOVE_SERIES)) {
            study = new StudyResponse();
            study.seriesResponses = new ArrayList<>();
            for(Series eventSeries : mutation.getSeries()) {
                SeriesResponse seriesResponse = new SeriesResponse();
                seriesResponse.seriesUID = eventSeries.getSeriesInstanceUID();
                seriesResponse.seriesDescription = eventSeries.getSeriesDescription();
                study.seriesResponses.add(seriesResponse);
                seriesResponse.isPresentInAlbum = mutation.getAlbum().containsSeries(eventSeries, em);
            }
            study.studyUID = mutation.getStudy().getStudyInstanceUID();
            study.studyDescription = mutation.getStudy().getStudyDescription();
            mutation.getReportProvider().ifPresent(mutationReportProvider ->
                    source.setReportProvider(mutationReportProvider, ReportProviderResponse.Type.EVENT));
        }
        if (mutationType_.equals(MutationType.ADD_FAV) ||
                mutationType_.equals(MutationType.REMOVE_FAV)) {
            study = new StudyResponse();
            study.seriesResponses = new ArrayList<>();
            for(Series eventSeries : mutation.getSeries()) {
                SeriesResponse seriesResponse = new SeriesResponse();
                seriesResponse.seriesUID = eventSeries.getSeriesInstanceUID();
                seriesResponse.seriesDescription = eventSeries.getSeriesDescription();
                study.seriesResponses.add(seriesResponse);
                seriesResponse.isPresentInAlbum = mutation.getAlbum().containsSeries(eventSeries, em);
            }

            study.studyUID = mutation.getStudy().getStudyInstanceUID();
            study.studyDescription = mutation.getStudy().getStudyDescription();
        }
        if (mutationType_.equals(MutationType.IMPORT_STUDY) ||
                mutationType_.equals(MutationType.REMOVE_STUDY)) {
            study = new StudyResponse();
            study.studyUID = mutation.getStudy().getStudyInstanceUID();
            study.studyDescription = mutation.getStudy().getStudyDescription();
            study.seriesResponses = new ArrayList<>();
            for(Series eventSeries : mutation.getSeries()) {
                SeriesResponse seriesResponse = new SeriesResponse();
                seriesResponse.seriesUID = eventSeries.getSeriesInstanceUID();
                seriesResponse.seriesDescription = eventSeries.getSeriesDescription();
                study.seriesResponses.add(seriesResponse);
                seriesResponse.isPresentInAlbum = mutation.getAlbum().containsSeries(eventSeries, em);
            }
        }

        if (mutationType_.equals(MutationType.CREATE_REPORT_PROVIDER) ||
                mutationType_.equals(MutationType.DELETE_REPORT_PROVIDER) ||
                mutationType_.equals(MutationType.EDIT_REPORT_PROVIDER)) {
            mutation.getReportProvider().ifPresent(mutationReportProvider ->
                    reportProvider = new ReportProviderResponse(mutationReportProvider, ReportProviderResponse.Type.EVENT));
        }

        mutation.getCapability().ifPresent(mutationCapability -> source.setCapabilityToken(mutationCapability));
    }
}
