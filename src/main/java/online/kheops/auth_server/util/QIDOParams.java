package online.kheops.auth_server.util;

import online.kheops.auth_server.KheopsPrincipalInterface;
import online.kheops.auth_server.NotAlbumScopeTypeException;
import online.kheops.auth_server.album.AlbumForbiddenException;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.user.UserPermissionEnum;
import org.dcm4che3.data.Tag;

import javax.ws.rs.core.MultivaluedMap;
import java.util.*;

import static online.kheops.auth_server.util.Consts.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class QIDOParams {
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
    private final Optional<String> studyInstanceUIDFilter;
    private final Optional<String> studyIDFilter;

    public QIDOParams(KheopsPrincipalInterface kheopsPrincipal, MultivaluedMap<String, String> queryParameters) throws BadQueryParametersException, AlbumNotFoundException, AlbumForbiddenException {
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
                throw new BadQueryParametersException("from inbox forbidden with a capability token with an album scope");//todo good message
            }
            try {
                albumIDLocal = kheopsPrincipal.getAlbumID();
            } catch (NotAlbumScopeTypeException notUsed) { /*empty*/ }

            if (!kheopsPrincipal.hasAlbumPermission(UserPermissionEnum.READ_SERIES, albumIDLocal)) {
                throw new AlbumForbiddenException("Token doesn't have read access");
            }
        }
        albumID = Optional.ofNullable(albumIDLocal);
        fromInbox = fromInboxLocal;
        if (queryParameters.containsKey(QUERY_PARAMETER_SORT)) {
            descending = queryParameters.get(QUERY_PARAMETER_SORT).get(0).startsWith("-");
            final String orderByParameter = queryParameters.get(QUERY_PARAMETER_SORT).get(0).replace("-", "");
            orderByTag = org.dcm4che3.util.TagUtils.forName(orderByParameter);
            if (orderByTag == -1 || !ACCEPTED_TAGS_FOR_SORTING.contains(orderByTag)) {
                throw new BadQueryParametersException("sort: " + queryParameters.get(QUERY_PARAMETER_SORT).get(0));
            }
        } else {
            descending = true;
            orderByTag = Tag.StudyDate;
        }


        if (queryParameters.containsKey(Consts.QUERY_PARAMETER_LIMIT)) {
            limit = JOOQTools.getLimit(queryParameters);
        } else {
            limit = OptionalInt.empty();
        }
        if (queryParameters.containsKey(Consts.QUERY_PARAMETER_OFFSET)) {
            offset = JOOQTools.getOffset(queryParameters);
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
        studyInstanceUIDFilter = getFilter(Tag.StudyInstanceUID, queryParameters);
        studyIDFilter = getFilter(Tag.StudyID, queryParameters);

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

    public Optional<String> getStudyInstanceUIDFilter() {
        return studyInstanceUIDFilter;
    }

    public Optional<String> getStudyIDFilter() {
        return studyIDFilter;
    }

    public boolean isFuzzyMatching() {
        return fuzzyMatching;
    }

    private static Optional<String> getFilter(int tag, MultivaluedMap<String, String> queryParameters) {
        if (queryParameters.containsKey(org.dcm4che3.data.Keyword.valueOf(tag))) {
            return Optional.ofNullable(queryParameters.get(org.dcm4che3.data.Keyword.valueOf(tag)).get(0));
        } else if (queryParameters.containsKey(String.format("%08X", tag))) {
            return Optional.ofNullable(queryParameters.get(String.format("%08X", tag)).get(0));
        } else {
            return Optional.empty();
        }
    }
}
