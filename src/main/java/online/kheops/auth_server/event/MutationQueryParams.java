package online.kheops.auth_server.event;

import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.capability.CapabilityNotFoundException;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.principal.ReportProviderPrincipal;
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
import static online.kheops.auth_server.series.Series.getSeries;
import static online.kheops.auth_server.study.Studies.getStudy;
import static online.kheops.auth_server.user.Users.getUser;
import static online.kheops.auth_server.util.ErrorResponse.Message.BAD_QUERY_PARAMETER;

public class MutationQueryParams {

    private static final String[] ACCEPTED_VALUES_FOR_FAMILIES_ARRAY = {"webhooks", "sending", "users"};
    private static final Set<String> ACCEPTED_VALUES_FOR_FAMILIES = new HashSet<>(Arrays.asList(ACCEPTED_VALUES_FOR_FAMILIES_ARRAY));

    private List<String> types;
    private List<String> families;
    private List<String> users;
    private List<String> studies;
    private List<String> series;
    private List<String> capabilityTokens;
    private List<String> reportProviders;
    private Optional<String> startDate;
    private Optional<String> endDate;


    public MutationQueryParams(KheopsPrincipal kheopsPrincipal, MultivaluedMap<String, String> queryParameters, String albumId, EntityManager em)
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
        final List<String> lst = new ArrayList<>();
        if (queryParameters.containsKey("type")) {
            for (String type:queryParameters.get("type")) {
                try {
                    Events.MutationType.valueOf(type);
                    lst.add(type);
                } catch (IllegalArgumentException e) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(BAD_QUERY_PARAMETER)
                            .detail("'type' is not valid")
                            .build();
                    throw new BadQueryParametersException(errorResponse);
                }
            }
        }
        types = lst;
    }

    private void extractFamilies(MultivaluedMap<String, String> queryParameters)
            throws BadQueryParametersException {
        final List<String> lst = new ArrayList<>();
        if (queryParameters.containsKey("family")) {
            for (String family:queryParameters.get("family")) {
                if (ACCEPTED_VALUES_FOR_FAMILIES.contains(family)) {
                    lst.add(family);
                } else {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(BAD_QUERY_PARAMETER)
                            .detail("'family' is not valid")
                            .build();
                    throw new BadQueryParametersException(errorResponse);
                }
            }
        }
        families = lst;
    }

    private void extractUsers(MultivaluedMap<String, String> queryParameters, EntityManager em)
            throws BadQueryParametersException {
        final List<String> lst = new ArrayList<>();
        if (queryParameters.containsKey("user")) {
            for (String user:queryParameters.get("user")) {
                try {
                    final User user1 = getUser(user, em);
                    lst.add(user1.getSub());
                } catch (UserNotFoundException e) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(BAD_QUERY_PARAMETER)
                            .detail("'user' not found")
                            .build();
                    throw new BadQueryParametersException(errorResponse);
                }
            }
        }
        users = lst;
    }

    private void extractStudies(MultivaluedMap<String, String> queryParameters, EntityManager em)
            throws BadQueryParametersException {
        final List<String> lst = new ArrayList<>();
        if (queryParameters.containsKey("studies")) {
            for (String study:queryParameters.get("studies")) {
                try {
                    getStudy(study, em);
                    lst.add(study);
                } catch (StudyNotFoundException e) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(BAD_QUERY_PARAMETER)
                            .detail("'studies' not found")
                            .build();
                    throw new BadQueryParametersException(errorResponse);
                }
            }
        }
        studies = lst;
    }

    private void extractSeries(MultivaluedMap<String, String> queryParameters, EntityManager em)
            throws BadQueryParametersException {
        final List<String> lst = new ArrayList<>();
        if (queryParameters.containsKey("series")) {
            for (String series:queryParameters.get("series")) {
                try {
                    Series.getSeries(series, em);
                    lst.add(series);
                } catch (SeriesNotFoundException e) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(BAD_QUERY_PARAMETER)
                            .detail("'series' not found")
                            .build();
                    throw new BadQueryParametersException(errorResponse);
                }
            }
        }
        series = lst;
    }

    private void extractReportProviders(MultivaluedMap<String, String> queryParameters, String albumId, EntityManager em)
            throws BadQueryParametersException {
        final List<String> lst = new ArrayList<>();
        if (queryParameters.containsKey("reportProvider")) {
            for (String reportProvider:queryParameters.get("reportProvider")) {
                try {
                    getReportProviderWithClientIdAndAlbumId(reportProvider, albumId, em);
                    lst.add(reportProvider);
                } catch (ReportProviderNotFoundException e) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(BAD_QUERY_PARAMETER)
                            .detail("'reportProvider' not found")
                            .build();
                    throw new BadQueryParametersException(errorResponse);
                }
            }
        }
        reportProviders = lst;
    }

    private void extractCapabilityToken(MultivaluedMap<String, String> queryParameters, String albumId, EntityManager em)
            throws BadQueryParametersException {
        final List<String> lst = new ArrayList<>();
        if (queryParameters.containsKey("capabilityToken")) {
            for (String capabilityToken:queryParameters.get("capabilityToken")) {
                try {
                    findCapabilityByCapabilityIDAndAlbumId(capabilityToken, albumId, em);
                    lst.add(capabilityToken);
                } catch (CapabilityNotFoundException e) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(BAD_QUERY_PARAMETER)
                            .detail("'capabilityToken' not found")
                            .build();
                    throw new BadQueryParametersException(errorResponse);
                }
            }
        }
        capabilityTokens = lst;
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


    public List<String> getTypes() { return types; }
    public List<String> getFamilies() { return families; }
    public List<String> getUsers() { return users; }
    public List<String> getStudies() { return studies; }
    public List<String> getSeries() { return series; }
    public List<String> getCapabilityTokens() { return capabilityTokens; }
    public List<String> getReportProviders() { return reportProviders; }
    public Optional<String> getStartDate() { return startDate; }
    public Optional<String> getEndDate() { return endDate; }
}
