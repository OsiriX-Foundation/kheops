package online.kheops.auth_server.util;

import online.kheops.auth_server.NotAlbumScopeTypeException;
import online.kheops.auth_server.album.AlbumForbiddenException;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.user.AlbumUserPermissions;
import org.dcm4che3.data.Keyword;
import org.dcm4che3.data.Tag;

import javax.persistence.NoResultException;
import javax.ws.rs.core.MultivaluedMap;
import java.util.*;

import static online.kheops.auth_server.util.Consts.*;
import static online.kheops.auth_server.util.ErrorResponse.Message.AUTHORIZATION_ERROR;
import static online.kheops.auth_server.util.ErrorResponse.Message.BAD_QUERY_PARAMETER;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class StudyQIDOParams {
    private static final Integer[] ACCEPTED_TAGS_FOR_SORTING_ARRAY = {Tag.StudyDate, Tag.StudyTime, Tag.AccessionNumber, Tag.ReferringPhysicianName, Tag.PatientName, Tag.PatientID, Tag.StudyInstanceUID, Tag.StudyID};
    private static final Set<Integer> ACCEPTED_TAGS_FOR_SORTING = new HashSet<>(Arrays.asList(ACCEPTED_TAGS_FOR_SORTING_ARRAY));

    private final Optional<String> albumID;
    private final boolean fromInbox;

    private final boolean descending;
    private final int orderByTag;

    private final boolean fuzzyMatching;

    private final OptionalInt limit;
    private final OptionalInt offset;

    private final Optional<String> studyDateFilter;
    private final Optional<String> studyTimeFilter;
    private final Optional<String> accessionNumberFilter;
    private final Optional<String> modalityFilter;
    private final Optional<String> referringPhysicianNameFilter;
    private final Optional<String> patientNameFilter;
    private final Optional<String> patientIDFilter;
    private final Optional<String> studyIDFilter;
    private final Optional<String> studyDescriptionFilter;
    private final Optional<Boolean> favoriteFilter;
    private List<String> studyInstanceUIDFilter;

    private final boolean favoriteField;
    private final boolean commentField;
    private final boolean studyDescriptionField;

    public StudyQIDOParams(KheopsPrincipal kheopsPrincipal, MultivaluedMap<String, String> queryParameters)
            throws BadQueryParametersException, AlbumForbiddenException , NoResultException {

        String albumIDLocal = null;
        boolean fromInboxLocal = false;

        if (queryParameters.containsKey(ALBUM)) {
            albumIDLocal = queryParameters.get(ALBUM).get(0);
        }

        if (queryParameters.containsKey(INBOX)) {
            fromInboxLocal = true;
        }

        if(kheopsPrincipal.getScope() == ScopeType.ALBUM) {
            if(fromInboxLocal) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(BAD_QUERY_PARAMETER)
                        .detail("from inbox forbidden with a capability token with an album scope")//todo good message
                        .build();
                throw new BadQueryParametersException(errorResponse);
            }
            try {
                albumIDLocal = kheopsPrincipal.getAlbumID();
            } catch (NotAlbumScopeTypeException | AlbumNotFoundException e) {
                throw new IllegalStateException(" Should not happen", e);
            }

            if (!kheopsPrincipal.hasAlbumPermission(AlbumUserPermissions.READ_SERIES, albumIDLocal)) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(AUTHORIZATION_ERROR)
                        .detail("Token doesn't have read access to this album")
                        .build();
                throw new AlbumForbiddenException(errorResponse);
            }
        }

        albumID = Optional.ofNullable(albumIDLocal);
        fromInbox = fromInboxLocal;
        if (queryParameters.containsKey(QUERY_PARAMETER_SORT)) {
            descending = queryParameters.get(QUERY_PARAMETER_SORT).get(0).startsWith("-");
            final String orderByParameter = queryParameters.get(QUERY_PARAMETER_SORT).get(0).replace("-", "");
            orderByTag = org.dcm4che3.util.TagUtils.forName(orderByParameter);
            if (orderByTag == -1 || !ACCEPTED_TAGS_FOR_SORTING.contains(orderByTag)) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(BAD_QUERY_PARAMETER)
                        .detail("'sort' query parameter is bad")
                        .build();
                throw new BadQueryParametersException(errorResponse);
            }
        } else {
            descending = true;
            orderByTag = Tag.StudyDate;
        }

        if (queryParameters.containsKey(Consts.QUERY_PARAMETER_LIMIT)) {
            limit = QueryParamTools.getLimit(queryParameters);
        } else {
            limit = OptionalInt.empty();
        }

        if (queryParameters.containsKey(Consts.QUERY_PARAMETER_OFFSET)) {
            offset = QueryParamTools.getOffset(queryParameters);
        } else {
            offset = OptionalInt.empty();
        }


        studyDateFilter = getFilter(Tag.StudyDate, queryParameters);
        studyTimeFilter = getFilter(Tag.StudyTime, queryParameters);
        accessionNumberFilter = getFilter(Tag.AccessionNumber, queryParameters);
        modalityFilter = getFilter(Tag.ModalitiesInStudy, queryParameters);
        referringPhysicianNameFilter = getFilter(Tag.ReferringPhysicianName, queryParameters);
        patientNameFilter = getFilter(Tag.PatientName, queryParameters);
        patientIDFilter = getFilter(Tag.PatientID, queryParameters);
        studyIDFilter = getFilter(Tag.StudyID, queryParameters);
        studyDescriptionFilter = getFilter(Tag.StudyDescription, queryParameters);

        String studyInstanceUIDFilterTmp = getFilter(Tag.StudyInstanceUID, queryParameters).orElse(null);
        List<String> studyInstanceUIDFilterTmpLst = kheopsPrincipal.getStudyList().orElse(new ArrayList<>());
        studyInstanceUIDFilter = new ArrayList<>();

        if(studyInstanceUIDFilterTmpLst.isEmpty() || studyInstanceUIDFilterTmp == null) {
            if(!studyInstanceUIDFilterTmpLst.isEmpty()) {
                studyInstanceUIDFilter.addAll(studyInstanceUIDFilterTmpLst);
            }
            if(studyInstanceUIDFilterTmp != null) {
                studyInstanceUIDFilter.add(studyInstanceUIDFilterTmp);
            }
        } else {
            if(studyInstanceUIDFilterTmpLst.contains(studyInstanceUIDFilterTmp)) {
                studyInstanceUIDFilter.add(studyInstanceUIDFilterTmp);
            }
            if (studyInstanceUIDFilter.isEmpty()) {
                throw new NoResultException();
            }
        }

        favoriteField = getFavoriteField(queryParameters);
        commentField = getCommentField(queryParameters);
        favoriteFilter = getFavoriteFilter(queryParameters);
        studyDescriptionField = getStudyDescriptionField(queryParameters);


        if(!albumID.isPresent() && !fromInbox) {
            if(favoriteField) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(BAD_QUERY_PARAMETER)
                        .detail("If include field favorite(0x0001,2345), you must specify "+INBOX+"=true OR "+ALBUM+"=XX as query param")
                        .build();
                throw new BadQueryParametersException(errorResponse);
            }
            if(favoriteFilter.isPresent()) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(BAD_QUERY_PARAMETER)
                        .detail("If favorite is set, you must specify "+INBOX+"=true OR "+ALBUM+"={albumId} as query param")
                        .build();
                throw new BadQueryParametersException(errorResponse);
            }
        }

        if (queryParameters.containsKey(QUERY_PARAMETER_FUZZY_MATCHING)) {
            fuzzyMatching = Boolean.parseBoolean(queryParameters.get(QUERY_PARAMETER_FUZZY_MATCHING).get(0));
        } else {
            fuzzyMatching = false;
        }
    }


    public Optional<String> getAlbumID() {
        return albumID;
    }

    public boolean isFromInbox() {
        return fromInbox;
    }

    public boolean isDescending() {
        return descending;
    }

    public int getOrderByTag() {
        return orderByTag;
    }

    public OptionalInt getLimit() {
        return limit;
    }

    public OptionalInt getOffset() {
        return offset;
    }

    public Optional<String> getStudyDateFilter() {
        return studyDateFilter;
    }

    public Optional<String> getStudyTimeFilter() {
        return studyTimeFilter;
    }

    public Optional<String> getAccessionNumberFilter() {
        return accessionNumberFilter;
    }

    public Optional<String> getModalityFilter() {
        return modalityFilter;
    }

    public Optional<String> getReferringPhysicianNameFilter() {
        return referringPhysicianNameFilter;
    }

    public Optional<String> getPatientNameFilter() {
        return patientNameFilter;
    }

    public Optional<String> getPatientIDFilter() {
        return patientIDFilter;
    }

    public List<String> getStudyInstanceUIDFilter() {
        return studyInstanceUIDFilter;
    }

    public Optional<String> getStudyIDFilter() {
        return studyIDFilter;
    }

    public Optional<String> getStudyDescriptionFilter() { return studyDescriptionFilter; }

    public Optional<Boolean> getFavoriteFilter() { return favoriteFilter; }

    public boolean isFuzzyMatching() {
        return fuzzyMatching;
    }

    public boolean includeFavoriteField() { return favoriteField; }

    public boolean includeCommentField() { return commentField; }

    public boolean includeStudyDescriptionField() { return studyDescriptionField; }

    private static Optional<String> getFilter(int tag, MultivaluedMap<String, String> queryParameters) {
        if (queryParameters.containsKey(Keyword.valueOf(tag))) {
            return Optional.ofNullable(queryParameters.get(Keyword.valueOf(tag)).get(0));
        } else if (queryParameters.containsKey(String.format("%08X", tag))) {
            return Optional.ofNullable(queryParameters.get(String.format("%08X", tag)).get(0));
        } else {
            return Optional.empty();
        }
    }

    private static boolean getFavoriteField(MultivaluedMap<String, String> queryParameters) {
        if (queryParameters.containsKey(INCLUDE_FIELD)) {
            return queryParameters.get(INCLUDE_FIELD).contains(String.format("%08X", CUSTOM_DICOM_TAG_FAVORITE)) || queryParameters.get(INCLUDE_FIELD).contains(FAVORITE);
        } else {
            return false;
        }
    }

    private Optional<Boolean> getFavoriteFilter(MultivaluedMap<String, String> queryParameters) throws BadQueryParametersException{
        if (queryParameters.containsKey(FAVORITE)) {
            if(!Boolean.TRUE.equals(Boolean.valueOf(queryParameters.get(FAVORITE).get(0)))) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(BAD_QUERY_PARAMETER)
                        .detail("'favorite' filter can only be true")
                        .build();
                throw new BadQueryParametersException(errorResponse);
            }
            return Optional.of(true);
        } else {
            return Optional.empty();
        }
    }

    private static boolean getCommentField(MultivaluedMap<String, String> queryParameters) {
        if (queryParameters.containsKey(INCLUDE_FIELD)) {
            return queryParameters.get(INCLUDE_FIELD).contains(String.format("%08X", CUSTOM_DICOM_TAG_COMMENTS)) || queryParameters.get(INCLUDE_FIELD).contains(COMMENTS);
        } else {
            return false;
        }
    }

    private static boolean getStudyDescriptionField(MultivaluedMap<String, String> queryParameters) {
        if (queryParameters.containsKey(INCLUDE_FIELD)) {
            return queryParameters.get(INCLUDE_FIELD).contains(Keyword.valueOf(Tag.StudyDescription)) ||
                    queryParameters.get(INCLUDE_FIELD).contains(String.format("%08X", Tag.StudyDescription));
        } else {
            return false;
        }
    }
}
