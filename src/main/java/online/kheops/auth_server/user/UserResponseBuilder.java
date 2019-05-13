package online.kheops.auth_server.user;

public class UserResponseBuilder {
    private String email;
    private String sub;
    private Boolean albumAccess;
    private Boolean studyAccess;

    public UserResponseBuilder() {/*empty*/}

    public UserResponseBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    public UserResponseBuilder setSub(String sub) {
        this.sub = sub;
        return this;
    }

    public UserResponseBuilder setAlbumAccess(Boolean albumAccess) {
        this.albumAccess = albumAccess;
        return this;
    }

    public UserResponseBuilder setStudyAccess(Boolean studyAccess) {
        this.studyAccess = studyAccess;
        return this;
    }

    public UserResponse build() {
        return new UserResponse(this);
    }

    public String getEmail() { return email; }

    public String getSub() { return sub; }

    protected Boolean getAlbumAccess() { return albumAccess; }

    protected Boolean getStudyAccess() { return studyAccess; }
}
