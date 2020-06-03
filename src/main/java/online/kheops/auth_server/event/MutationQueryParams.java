package online.kheops.auth_server.event;

import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.capability.CapabilityNotFoundException;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.report_provider.ReportProviderNotFoundException;
import online.kheops.auth_server.series.Series;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.ErrorResponse;

import javax.persistence.EntityManager;
import javax.ws.rs.core.MultivaluedMap;
import java.util.*;

import static online.kheops.auth_server.capability.CapabilitiesQueries.findCapabilityByCapabilityIDAndAlbumId;
import static online.kheops.auth_server.report_provider.ReportProviderQueries.getReportProviderWithClientIdAndAlbumId;
import static online.kheops.auth_server.study.Studies.getStudy;
import static online.kheops.auth_server.user.Users.getUser;
import static online.kheops.auth_server.util.ErrorResponse.Message.BAD_QUERY_PARAMETER;

public class MutationQueryParams {

    private static final String[] ACCEPTED_VALUES_FOR_FAMILIES_ARRAY = {"webhooks", "sending", "users"};
    private static final Set<String> ACCEPTED_VALUES_FOR_FAMILIES = new HashSet<>(Arrays.asList(ACCEPTED_VALUES_FOR_FAMILIES_ARRAY));

    public enum families {
        WEBHOOKS {
            @Override
            public void addTypes(MutationQueryParams mutationQueryParams) {
                mutationQueryParams.types.add(Events.MutationType.CREATE_WEBHOOK);
                mutationQueryParams.types.add(Events.MutationType.DELETE_WEBHOOK);
                mutationQueryParams.types.add(Events.MutationType.EDIT_WEBHOOK);
                mutationQueryParams.types.add(Events.MutationType.TRIGGER_WEBHOOK);
            }
        },
        SENDING{
            @Override
            public void addTypes(MutationQueryParams mutationQueryParams) {
                mutationQueryParams.types.add(Events.MutationType.IMPORT_SERIES);
                mutationQueryParams.types.add(Events.MutationType.IMPORT_STUDY);
                mutationQueryParams.types.add(Events.MutationType.REMOVE_SERIES);
                mutationQueryParams.types.add(Events.MutationType.REMOVE_STUDY);
            }
        },
        USERS{
            @Override
            public void addTypes (MutationQueryParams mutationQueryParams) {
                mutationQueryParams.types.add(Events.MutationType.ADD_USER);
                mutationQueryParams.types.add(Events.MutationType.REMOVE_USER);
                mutationQueryParams.types.add(Events.MutationType.ADD_ADMIN);
                mutationQueryParams.types.add(Events.MutationType.PROMOTE_ADMIN);
                mutationQueryParams.types.add(Events.MutationType.DEMOTE_ADMIN);
                mutationQueryParams.types.add(Events.MutationType.LEAVE_ALBUM);
            }
        };

        public abstract void addTypes(MutationQueryParams mutationQueryParams);
    }

    private List<Events.MutationType> types = new ArrayList<>();
    private List<String> users = new ArrayList<>();
    private List<String> studies = new ArrayList<>();
    private List<String> series = new ArrayList<>();
    private List<String> capabilityTokens = new ArrayList<>();
    private List<String> reportProviders = new ArrayList<>();
    private Optional<String> startDate;
    private Optional<String> endDate;


    public MutationQueryParams(MultivaluedMap<String, String> queryParameters, String albumId, EntityManager em)
            throws BadQueryParametersException {

        extractTypes(queryParameters);
        extractFamilies(queryParameters);
        extractUsers(queryParameters, em);
        extractStudies(queryParameters, em);
        extractSeries(queryParameters, em);
        extractReportProviders(queryParameters, albumId, em);
        extractCapabilityToken(queryParameters, albumId, em);
        extractDate(queryParameters);

    }

    private void extractTypes(MultivaluedMap<String, String> queryParameters)
            throws BadQueryParametersException {

        if (queryParameters.containsKey("type")) {
            for (String type:queryParameters.get("type")) {
                try {
                    types.add(Events.MutationType.valueOf(type));
                } catch (IllegalArgumentException e) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(BAD_QUERY_PARAMETER)
                            .detail("'type' is not valid")
                            .build();
                    throw new BadQueryParametersException(errorResponse);
                }
            }
        }
    }

    private void extractFamilies(MultivaluedMap<String, String> queryParameters)
            throws BadQueryParametersException {
        if (queryParameters.containsKey("family")) {
            for (String family:queryParameters.get("family")) {
                if (ACCEPTED_VALUES_FOR_FAMILIES.contains(family)) {
                    families.valueOf(family.toUpperCase()).addTypes(this);
                } else {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(BAD_QUERY_PARAMETER)
                            .detail("'family' is not valid")
                            .build();
                    throw new BadQueryParametersException(errorResponse);
                }
            }
        }
    }

    private void extractUsers(MultivaluedMap<String, String> queryParameters, EntityManager em)
            throws BadQueryParametersException {
        if (queryParameters.containsKey("user")) {
            for (String user:queryParameters.get("user")) {
                try {
                    final User user1 = getUser(user, em);
                    users.add(user1.getSub());
                } catch (UserNotFoundException e) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(BAD_QUERY_PARAMETER)
                            .detail("'user' not found")
                            .build();
                    throw new BadQueryParametersException(errorResponse);
                }
            }
        }
    }

    private void extractStudies(MultivaluedMap<String, String> queryParameters, EntityManager em)
            throws BadQueryParametersException {
        if (queryParameters.containsKey("studies")) {
            for (String study:queryParameters.get("studies")) {
                try {
                    getStudy(study, em);
                    studies.add(study);
                } catch (StudyNotFoundException e) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(BAD_QUERY_PARAMETER)
                            .detail("'studies' not found")
                            .build();
                    throw new BadQueryParametersException(errorResponse);
                }
            }
        }
    }

    private void extractSeries(MultivaluedMap<String, String> queryParameters, EntityManager em)
            throws BadQueryParametersException {
        if (queryParameters.containsKey("series")) {
            for (String seriesUID:queryParameters.get("series")) {
                try {
                    Series.getSeries(seriesUID, em);
                    series.add(seriesUID);
                } catch (SeriesNotFoundException e) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(BAD_QUERY_PARAMETER)
                            .detail("'series' not found")
                            .build();
                    throw new BadQueryParametersException(errorResponse);
                }
            }
        }
    }

    private void extractReportProviders(MultivaluedMap<String, String> queryParameters, String albumId, EntityManager em)
            throws BadQueryParametersException {
        if (queryParameters.containsKey("reportProvider")) {
            for (String reportProvider:queryParameters.get("reportProvider")) {
                try {
                    getReportProviderWithClientIdAndAlbumId(reportProvider, albumId, em);
                    reportProviders.add(reportProvider);
                } catch (ReportProviderNotFoundException e) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(BAD_QUERY_PARAMETER)
                            .detail("'reportProvider' not found")
                            .build();
                    throw new BadQueryParametersException(errorResponse);
                }
            }
        }
    }

    private void extractCapabilityToken(MultivaluedMap<String, String> queryParameters, String albumId, EntityManager em)
            throws BadQueryParametersException {
        if (queryParameters.containsKey("capabilityToken")) {
            for (String capabilityToken:queryParameters.get("capabilityToken")) {
                try {
                    findCapabilityByCapabilityIDAndAlbumId(capabilityToken, albumId, em);
                    capabilityTokens.add(capabilityToken);
                } catch (CapabilityNotFoundException e) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(BAD_QUERY_PARAMETER)
                            .detail("'capabilityToken' not found")
                            .build();
                    throw new BadQueryParametersException(errorResponse);
                }
            }
        }
    }

    private void extractDate(MultivaluedMap<String, String> queryParameters)
            throws BadQueryParametersException {
        if (queryParameters.containsKey("startDate")) {
            startDate = Optional.ofNullable(queryParameters.get("startDate").get(0));
        }
        if (queryParameters.containsKey("startDate")) {
            endDate = Optional.ofNullable(queryParameters.get("endDate").get(0));
        }
    }


    public List<Events.MutationType> getTypes() { return types; }
    public List<String> getUsers() { return users; }
    public List<String> getStudies() { return studies; }
    public List<String> getSeries() { return series; }
    public List<String> getCapabilityTokens() { return capabilityTokens; }
    public List<String> getReportProviders() { return reportProviders; }
    public Optional<String> getStartDate() { return startDate; }
    public Optional<String> getEndDate() { return endDate; }
}
