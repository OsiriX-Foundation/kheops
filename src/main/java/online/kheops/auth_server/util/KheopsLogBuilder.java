package online.kheops.auth_server.util;

import javafx.util.Pair;
import online.kheops.auth_server.user.UsersPermission;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class KheopsLogBuilder {

    public enum ActionType {LIST_ALBUMS, LIST_USERS, NEW_ALBUM, EDIT_ALBUM, ADD_USER, REMOVE_USER, ADD_ADMIN, REMOVE_ADMIN, ADD_FAVORITE, REMOVE_FAVORITE, DELETE_ALBUM, GET_ALBUM,
        SHARE_STUDY_WITH_USER, SHARE_SERIES_WITH_USER, SHARE_STUDY_WITH_ALBUM, REMOVE_SERIES, REMOVE_STUDY}

    private ArrayList<Pair<String,String>> log;
    private static final Logger LOG = Logger.getLogger(KheopsLogBuilder.class.getName());


    public KheopsLogBuilder() {
        log = new ArrayList<>();
    }

    public KheopsLogBuilder user(String userId) {
        log.add(new Pair<>("user", userId));
        return this;
    }
    public KheopsLogBuilder targetUser(String userId) {
        log.add(new Pair<>("target_user", userId));
        return this;
    }
    public KheopsLogBuilder album(String albumId) {
        log.add(new Pair<>("album", albumId));
        return this;
    }
    public KheopsLogBuilder fromAlbum(String albumId) {
        log.add(new Pair<>("fromAlbum", albumId));
        return this;
    }
    public KheopsLogBuilder action(ActionType action) {
        log.add(new Pair<>("action", action.name()));
        return this;
    }
    public KheopsLogBuilder userPermission(UsersPermission usersPermission) {
        usersPermission.getAddSeries().ifPresent(addSeries -> log.add(new Pair<>("addSeries", addSeries.toString())));
        usersPermission.getAddUser().ifPresent(addUser -> log.add(new Pair<>("addUser", addUser.toString())));
        usersPermission.getDeleteSeries().ifPresent(deleteSeries -> log.add(new Pair<>("deleteSeries", deleteSeries.toString())));
        usersPermission.getDownloadSeries().ifPresent(downloarSeries -> log.add(new Pair<>("downloadSeries", downloarSeries.toString())));
        usersPermission.getSendSeries().ifPresent(sendSeries -> log.add(new Pair<>("sendSeries", sendSeries.toString())));
        usersPermission.getWriteComments().ifPresent(writeComments -> log.add(new Pair<>("writeComments", writeComments.toString())));
        return this;
    }

    public KheopsLogBuilder study(String studyUID) {
        log.add(new Pair<>("studyUID", studyUID));
        return this;
    }
    public KheopsLogBuilder series(String seriesUID) {
        log.add(new Pair<>("seriesUID", seriesUID));
        return this;
    }
    public KheopsLogBuilder fromInbox(Boolean fromInbox) {
        log.add(new Pair<>("fromInbox", fromInbox.toString()));
        return this;
    }


    public void log() {
        String logString = "";
        for (Pair<String, String> pair:log) {
            logString += pair.getKey() + "=" + pair.getValue() + " ";
        }
        LOG.log(KheopsLevel.KHEOPS, logString);
    }


}
