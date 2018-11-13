package online.kheops.auth_server.util;

import online.kheops.auth_server.KheopsPrincipalInterface;
import online.kheops.auth_server.NotAlbumScopeTypeException;
import online.kheops.auth_server.album.AlbumForbiddenException;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.user.UsersPermission;
import org.dcm4che3.data.Tag;

import javax.ws.rs.core.MultivaluedMap;
import java.util.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class QIDOParams {

    private static final Integer[] ACCEPTED_TAGS_FOR_SORTING_ARRAY = {Tag.StudyDate, Tag.StudyTime, Tag.AccessionNumber, Tag.ReferringPhysicianName, Tag.PatientName, Tag.PatientID, Tag.StudyInstanceUID, Tag.StudyID};
    private static final Set<Integer> ACCEPTED_TAGS_FOR_SORTING = new HashSet<>(Arrays.asList(ACCEPTED_TAGS_FOR_SORTING_ARRAY));

    private final Long album_id;
    private final boolean fromInbox;

    private final boolean descending;
    private final int orderByTag;

    private final boolean fuzzyMatching;

    private final OptionalInt limit;
    private final OptionalInt offset;

    private final String studyDateFilter;
    private final String studyTimeFilter;
    private final String accessionNumberFilter;
    private final String modalityFilter;
    private final String referringPhysicianNameFilter;
    private final String patientNameFilter;
    private final String patientIDFilter;
    private final String studyInstanceUIDFilter;
    private final String studyIDFilter;

    public QIDOParams(KheopsPrincipalInterface kheopsPrincipal, MultivaluedMap<String, String> queryParameters) throws BadQueryParametersException, AlbumNotFoundException, AlbumForbiddenException{
        Long album_id = null;
        boolean fromInbox = false;
        if (queryParameters.containsKey("album")) {
            album_id = Long.parseLong(queryParameters.get("album").get(0));
        }
        if (queryParameters.containsKey("inbox")) {
            fromInbox = true;
        }
        if(kheopsPrincipal.getScope() == ScopeType.ALBUM) {
            if (kheopsPrincipal.hasAlbumPermission(UsersPermission.UsersPermissionEnum.READ_SERIES, album_id)) {
                fromInbox = false;
                try {
                    album_id = kheopsPrincipal.getAlbumID();
                } catch (NotAlbumScopeTypeException notUsed) { /*empty*/ }
            } else {
                throw new AlbumForbiddenException("Token doesn't have read access");
            }
        }
        this.album_id = album_id;
        this.fromInbox = fromInbox;
        if (queryParameters.containsKey("sort")) {
            descending = queryParameters.get("sort").get(0).startsWith("-");
            final String orderByParameter = queryParameters.get("sort").get(0).replace("-", "");
            orderByTag = org.dcm4che3.util.TagUtils.forName(orderByParameter);
            if (orderByTag == -1 || !ACCEPTED_TAGS_FOR_SORTING.contains(orderByTag)) {
                throw new BadQueryParametersException("sort: " + queryParameters.get("sort").get(0));
            }
        } else {
            descending = true;
            orderByTag = Tag.StudyDate;
        }

        limit = getLimit(queryParameters);
        offset = getOffset(queryParameters);

        studyDateFilter = getFilter(Tag.StudyDate, queryParameters);
        studyTimeFilter = getFilter(Tag.StudyTime, queryParameters);
        accessionNumberFilter = getFilter(Tag.AccessionNumber, queryParameters);
        modalityFilter = getFilter(Tag.ModalitiesInStudy, queryParameters);
        referringPhysicianNameFilter = getFilter(Tag.ReferringPhysicianName, queryParameters);
        patientNameFilter = getFilter(Tag.PatientName, queryParameters);
        patientIDFilter = getFilter(Tag.PatientID, queryParameters);
        studyInstanceUIDFilter = getFilter(Tag.StudyInstanceUID, queryParameters);
        studyIDFilter = getFilter(Tag.StudyID, queryParameters);

        if (queryParameters.containsKey("fuzzyMatching")) {
            fuzzyMatching = Boolean.parseBoolean(queryParameters.get("fuzzyMatching").get(0));
        } else {
            fuzzyMatching = false;
        }
    }

    public OptionalLong getAlbum_id() {
        return Optional.ofNullable(album_id).map(OptionalLong::of).orElseGet(OptionalLong::empty);
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
        return Optional.ofNullable(studyDateFilter);
    }

    public Optional<String> getStudyTimeFilter() {
        return Optional.ofNullable(studyTimeFilter);
    }

    public Optional<String> getAccessionNumberFilter() {
        return Optional.ofNullable(accessionNumberFilter);
    }

    public Optional<String> getModalityFilter() {
        return Optional.ofNullable(modalityFilter);
    }

    public Optional<String> getReferringPhysicianNameFilter() {
        return Optional.ofNullable(referringPhysicianNameFilter);
    }

    public Optional<String> getPatientNameFilter() {
        return Optional.ofNullable(patientNameFilter);
    }

    public Optional<String> getPatientIDFilter() {
        return Optional.ofNullable(patientIDFilter);
    }

    public Optional<String> getStudyInstanceUIDFilter() {
        return Optional.ofNullable(studyInstanceUIDFilter);
    }

    public Optional<String> getStudyIDFilter() {
        return Optional.ofNullable(studyIDFilter);
    }

    public boolean isFuzzyMatching() {
        return fuzzyMatching;
    }

    private static OptionalInt getLimit(MultivaluedMap<String, String> queryParameters) throws BadQueryParametersException {
        if (queryParameters.containsKey(Consts.QUERY_PARAMETER_LIMIT)) {
            final int limit;
            try {
                limit = Integer.parseInt(queryParameters.get(Consts.QUERY_PARAMETER_LIMIT).get(0));
            } catch (Exception e) {
                throw new BadQueryParametersException(Consts.QUERY_PARAMETER_LIMIT + ": " + queryParameters.get(Consts.QUERY_PARAMETER_LIMIT).get(0));
            }
            if (limit < 1) {
                throw new BadQueryParametersException(Consts.QUERY_PARAMETER_LIMIT + ": " + queryParameters.get(Consts.QUERY_PARAMETER_LIMIT).get(0));
            }
            return OptionalInt.of(limit);
        } else {
            return OptionalInt.empty();
        }
    }

    private static OptionalInt getOffset(MultivaluedMap<String, String> queryParameters) throws BadQueryParametersException{
        if (queryParameters.containsKey(Consts.QUERY_PARAMETER_OFFSET)) {
            final int offset;
            try {
                offset = Integer.parseInt(queryParameters.get(Consts.QUERY_PARAMETER_OFFSET).get(0));
            } catch (Exception e) {
                throw new BadQueryParametersException(Consts.QUERY_PARAMETER_OFFSET + ": " + queryParameters.get(Consts.QUERY_PARAMETER_OFFSET).get(0));
            }

            if (offset < 0) {
                throw new BadQueryParametersException(Consts.QUERY_PARAMETER_OFFSET + ": " + queryParameters.get(Consts.QUERY_PARAMETER_OFFSET).get(0));
            }
            return OptionalInt.of(offset);
        } else {
            return OptionalInt.empty();
        }
    }

    private String getFilter(int tag, MultivaluedMap<String, String> queryParameters) {
        if (queryParameters.containsKey(org.dcm4che3.data.Keyword.valueOf(tag))) {
            return queryParameters.get(org.dcm4che3.data.Keyword.valueOf(tag)).get(0);
        } else if (queryParameters.containsKey(String.format("%08X",tag))) {
            return queryParameters.get(String.format("%08X", tag)).get(0);
        } else {
            return null;
        }
    }
}
