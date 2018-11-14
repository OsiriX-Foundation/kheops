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

public class QIDOParams {

    private Long album_id = null;
    private boolean fromInbox = false;

    private boolean descending = true;
    private int orderByTag = Tag.StudyDate;

    private boolean fuzzyMatching = false;

    private Integer limit = null;
    private Integer offset = null;

    private String studyDateFilter = null;
    private String studyTimeFilter = null;
    private String accessionNumberFilter = null;
    private String modalityFilter = null;
    private String referringPhysicianNameFilter = null;
    private String patientNameFilter = null;
    private String patientIDFilter = null;
    private String studyInstanceUIDFilter = null;
    private String studyIDFilter = null;


    private final Integer[]acceptedTagForSortingArray = {Tag.StudyDate, Tag.StudyTime, Tag.AccessionNumber, Tag.ReferringPhysicianName, Tag.PatientName, Tag.PatientID, Tag.StudyInstanceUID, Tag.StudyID};
    private final Set<Integer> acceptedTagForSorting = new HashSet<Integer>(Arrays.asList(acceptedTagForSortingArray));


    public QIDOParams(KheopsPrincipalInterface kheopsPrincipal, MultivaluedMap<String, String> queryParameters) throws BadQueryParametersException, AlbumNotFoundException, AlbumForbiddenException{
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
        if (queryParameters.containsKey("sort")) {
            descending = queryParameters.get("sort").get(0).startsWith("-");
            final String orderByParameter = queryParameters.get("sort").get(0).replace("-", "");
            orderByTag = org.dcm4che3.util.TagUtils.forName(orderByParameter);
            if (orderByTag == -1 || !acceptedTagForSorting.contains(orderByTag)) {
                throw new BadQueryParametersException("sort: " + queryParameters.get("sort").get(0));
            }
        }
        if (queryParameters.containsKey(Consts.QUERY_PARAMETER_LIMIT)) {
            limit = JOOQTools.getLimit(queryParameters);
        }
        if (queryParameters.containsKey(Consts.QUERY_PARAMETER_OFFSET)) {
            offset = JOOQTools.getOffset(queryParameters);
        }

        if (queryParameters.containsKey(org.dcm4che3.data.Keyword.valueOf(Tag.StudyDate))) {
            studyDateFilter = queryParameters.get(org.dcm4che3.data.Keyword.valueOf(Tag.StudyDate)).get(0);
        } else if (queryParameters.containsKey(String.format("%08X",Tag.StudyDate))) {
            studyDateFilter = queryParameters.get(String.format("%08X",Tag.StudyDate)).get(0);
        }

        if (queryParameters.containsKey(org.dcm4che3.data.Keyword.valueOf(Tag.StudyTime))) {
            studyTimeFilter = queryParameters.get(org.dcm4che3.data.Keyword.valueOf(Tag.StudyTime)).get(0);
        } else if (queryParameters.containsKey(String.format("%08X",Tag.StudyTime))) {
            studyTimeFilter = queryParameters.get(String.format("%08X",Tag.StudyTime)).get(0);
        }

        if (queryParameters.containsKey(org.dcm4che3.data.Keyword.valueOf(Tag.AccessionNumber))) {
            accessionNumberFilter = queryParameters.get(org.dcm4che3.data.Keyword.valueOf(Tag.AccessionNumber)).get(0);
        } else if (queryParameters.containsKey(String.format("%08X",Tag.AccessionNumber))) {
            accessionNumberFilter = queryParameters.get(String.format("%08X",Tag.AccessionNumber)).get(0);
        }

        if (queryParameters.containsKey(org.dcm4che3.data.Keyword.valueOf(Tag.ModalitiesInStudy))) {
            modalityFilter = queryParameters.get(org.dcm4che3.data.Keyword.valueOf(Tag.ModalitiesInStudy)).get(0);
        } else if (queryParameters.containsKey(String.format("%08X",Tag.ModalitiesInStudy))) {
            modalityFilter = queryParameters.get(String.format("%08X",Tag.ModalitiesInStudy)).get(0);
        }

        if (queryParameters.containsKey(org.dcm4che3.data.Keyword.valueOf(Tag.ReferringPhysicianName))) {
            referringPhysicianNameFilter = queryParameters.get(org.dcm4che3.data.Keyword.valueOf(Tag.ReferringPhysicianName)).get(0);
        } else if (queryParameters.containsKey(String.format("%08X",Tag.ReferringPhysicianName))) {
            referringPhysicianNameFilter = queryParameters.get(String.format("%08X",Tag.ReferringPhysicianName)).get(0);
        }

        if (queryParameters.containsKey(org.dcm4che3.data.Keyword.valueOf(Tag.PatientName))) {
            patientNameFilter = queryParameters.get(org.dcm4che3.data.Keyword.valueOf(Tag.PatientName)).get(0);
        } else if (queryParameters.containsKey(String.format("%08X",Tag.PatientName))) {
            patientNameFilter = queryParameters.get(String.format("%08X",Tag.PatientName)).get(0);
        }

        if (queryParameters.containsKey(org.dcm4che3.data.Keyword.valueOf(Tag.PatientID))) {
            patientIDFilter = queryParameters.get(org.dcm4che3.data.Keyword.valueOf(Tag.PatientID)).get(0);
        } else if (queryParameters.containsKey(String.format("%08X",Tag.PatientID))) {
            patientIDFilter = queryParameters.get(String.format("%08X",Tag.PatientID)).get(0);
        }

        if (queryParameters.containsKey(org.dcm4che3.data.Keyword.valueOf(Tag.StudyInstanceUID))) {
            studyInstanceUIDFilter = queryParameters.get(org.dcm4che3.data.Keyword.valueOf(Tag.StudyInstanceUID)).get(0);
        } else if (queryParameters.containsKey(String.format("%08X",Tag.StudyInstanceUID))) {
            studyInstanceUIDFilter = queryParameters.get(String.format("%08X",Tag.StudyInstanceUID)).get(0);
        }

        if (queryParameters.containsKey(org.dcm4che3.data.Keyword.valueOf(Tag.StudyID))) {
            studyIDFilter = queryParameters.get(org.dcm4che3.data.Keyword.valueOf(Tag.StudyID)).get(0);
        } else if (queryParameters.containsKey(String.format("%08X",Tag.StudyID))) {
            studyIDFilter = queryParameters.get(String.format("%08X",Tag.StudyID)).get(0);
        }

        if (queryParameters.containsKey("fuzzyMatching")) {
            fuzzyMatching = Boolean.parseBoolean(queryParameters.get("fuzzyMatching").get(0));
        }
    }


    public Optional<Long> getAlbum_id() {
        return Optional.ofNullable(album_id);
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

    public Optional<Integer> getLimit() {
        return Optional.ofNullable(limit);
    }

    public Optional<Integer> getOffset() {
        return Optional.ofNullable(offset);
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

}
