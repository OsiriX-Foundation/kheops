package online.kheops.auth_server.user;

import java.util.Optional;

public class UsersPermission {

    private Boolean addUser = false;
    private Boolean downloadSeries = true;
    private Boolean sendSeries = true;
    private Boolean deleteSeries = false;
    private Boolean addSeries = true;
    private Boolean writeComments = true;

    public Optional<Boolean> getAddUser() {
        return Optional.ofNullable(addUser);
    }

    public void setAddUser(Boolean addUser) { this.addUser = addUser; }

    public Optional<Boolean> getDownloadSeries() {
        return Optional.ofNullable(downloadSeries);
    }

    public void setDownloadSeries(Boolean downloadSeries) {
        this.downloadSeries = downloadSeries;
    }

    public Optional<Boolean> getSendSeries() {
        return Optional.ofNullable(sendSeries);
    }

    public void setSendSeries(Boolean sendSeries) {
        this.sendSeries = sendSeries;
    }

    public Optional<Boolean> getDeleteSeries() {
        return Optional.ofNullable(deleteSeries);
    }

    public void setDeleteSeries(Boolean deleteSeries) {
        this.deleteSeries = deleteSeries;
    }

    public Optional<Boolean> getAddSeries() {
        return Optional.ofNullable(addSeries);
    }

    public void setAddSeries(Boolean addSeries) {
        this.addSeries = addSeries;
    }

    public Optional<Boolean> getWriteComments() {
        return Optional.ofNullable(writeComments);
    }

    public void setWriteComments(Boolean writeComments) {
        this.writeComments = writeComments;
    }

    public void setInboxPermission() {
        addUser = false;
        downloadSeries = true;
        sendSeries = true;
        deleteSeries = true;
        addSeries = true;
        writeComments = false;
    }

    public boolean areSet() {
        return (addUser != null || downloadSeries != null || sendSeries != null || deleteSeries != null || addSeries != null || writeComments != null);
    }

    @Override
    public String toString() {
        String string = "";
        if(addUser != null) string += " addUser="+addUser;
        if(downloadSeries != null) string += " downloadSeries="+downloadSeries;
        if(sendSeries != null) string += " sendSeries="+sendSeries;
        if(deleteSeries != null) string += " deleteSeries="+deleteSeries;
        if(addSeries != null) string += " addSeries="+addSeries;
        if(writeComments != null) string += " writeComments="+writeComments;

        return string;
    }
}
