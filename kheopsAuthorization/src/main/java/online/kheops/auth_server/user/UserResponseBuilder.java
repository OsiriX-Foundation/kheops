package online.kheops.auth_server.user;

import online.kheops.auth_server.entity.User;

import java.util.Optional;

public class UserResponseBuilder {
    private String email;
    private String name;
    private String sub;
    private Boolean albumAccess;
    private Boolean studyAccess;
    private Optional<Boolean> canAccess = Optional.empty();
    private Optional<Boolean> isAdmin = Optional.empty();

    public UserResponseBuilder() {/*empty*/}

    public UserResponseBuilder setUser(User user) {
        sub = user.getSub();
        name = user.getName();
        email = user.getEmail();
        return this;
    }

    public UserResponseBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    public UserResponseBuilder setSub(String sub) {
        this.sub = sub;
        return this;
    }

    public UserResponseBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public UserResponseBuilder setAlbumAccess(Boolean albumAccess) {
        this.albumAccess = albumAccess;
        return this;
    }
    public UserResponseBuilder isAdmin(Boolean isAdmin) {
        this.isAdmin =  Optional.ofNullable(isAdmin);
        return this;
    }

    public UserResponseBuilder setCanAccess(Boolean canAccess) {
        this.canAccess = Optional.ofNullable(canAccess);
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
    public String getName() { return name; }
    public String getSub() { return sub; }
    public Boolean getAlbumAccess() { return albumAccess; }
    public Boolean getStudyAccess() { return studyAccess; }
    public Optional<Boolean> getIsAdmin() { return isAdmin; }
    public Optional<Boolean> getCanAccess() { return canAccess; }
}