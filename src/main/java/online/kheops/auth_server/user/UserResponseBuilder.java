package online.kheops.auth_server.user;

import online.kheops.auth_server.entity.User;

import java.util.Optional;

public class UserResponseBuilder {
    private String email;
    private String firstName;
    private String lastName;
    private String sub;
    private Boolean albumAccess;
    private Boolean studyAccess;
    private Optional<Boolean> canAccess = Optional.empty();

    public UserResponseBuilder() {/*empty*/}

    public UserResponseBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    public UserResponseBuilder setSub(String sub) {
        this.sub = sub;
        return this;
    }
    public UserResponseBuilder setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }
    public UserResponseBuilder setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public UserResponseBuilder setUser(User user) {
        sub = user.getKeycloakId();
        lastName = user.getLastName();
        firstName = user.getFirstName();
        email = user.getEmail();

        return this;
    }


    public UserResponseBuilder setAlbumAccess(Boolean albumAccess) {
        this.albumAccess = albumAccess;
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
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }

    public String getSub() { return sub; }

    public Boolean getAlbumAccess() { return albumAccess; }

    public Boolean getStudyAccess() { return studyAccess; }

    public Optional<Boolean> getCanAccess() { return canAccess; }
}
