package online.kheops.auth_server.user;

public class UserResponseBuilder {
    private String email;
    private String sub;

    public UserResponseBuilder() {/*empty*/}

    public UserResponseBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    public UserResponseBuilder setSub(String sub) {
        this.sub = sub;
        return this;
    }

    public UserResponse build() {
        return new UserResponse(this);
    }

    protected String getEmail() { return email; }

    protected String getSub() { return sub; }
}
