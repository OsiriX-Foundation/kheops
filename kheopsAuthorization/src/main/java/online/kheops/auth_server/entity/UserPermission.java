package online.kheops.auth_server.entity;

import online.kheops.auth_server.user.UsersPermission;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class UserPermission {
    @Basic(optional = false)
    @Column(name = "add_user_permission")
    private boolean addUser;

    @Basic(optional = false)
    @Column(name = "download_series_permission")
    private boolean downloadSeries;

    @Basic(optional = false)
    @Column(name = "send_series_permission")
    private boolean sendSeries;

    @Basic(optional = false)
    @Column(name = "delete_series_permission")
    private boolean deleteSeries;

    @Basic(optional = false)
    @Column(name = "add_series_permission")
    private boolean addSeries;

    @Basic(optional = false)
    @Column(name = "write_comments_permission")
    private boolean writeComments;

    public UserPermission() { /*empty*/ }

    public UserPermission(UsersPermission usersPermission) {
        usersPermission.getAddUser().ifPresent(this::setAddUser);
        usersPermission.getDownloadSeries().ifPresent(this::setDownloadSeries);
        usersPermission.getSendSeries().ifPresent(this::setSendSeries);
        usersPermission.getDeleteSeries().ifPresent(this::setDeleteSeries);
        usersPermission.getAddSeries().ifPresent(this::setAddSeries);
        usersPermission.getWriteComments().ifPresent(this::setWriteComments);
    }

    public boolean isAddUser() { return addUser; }

    public void setAddUser( boolean addUser ) { this.addUser = addUser; }

    public boolean isDownloadSeries() { return downloadSeries; }

    public void setDownloadSeries( boolean downloadSeries ) { this.downloadSeries = downloadSeries; }

    public boolean isSendSeries() { return sendSeries; }

    public void setSendSeries( boolean sendSeries ) { this.sendSeries = sendSeries; }

    public boolean isDeleteSeries() { return deleteSeries; }

    public void setDeleteSeries( boolean deleteSeries ) { this.deleteSeries = deleteSeries; }

    public boolean isAddSeries() { return addSeries; }

    public void setAddSeries( boolean addSeries ) { this.addSeries = addSeries; }

    public boolean isWriteComments() { return writeComments; }

    public void setWriteComments( boolean writeComments ) { this.writeComments = writeComments; }
}
