package online.kheops.auth_server.util;

public enum TokenClientKind {
    REPORT_PROVIDER("report_provider_client"),
    INTERNAL("internal_client"),
    PUBLIC("public_client");

    private String roleString;

    TokenClientKind(String roleString) {
        this.roleString = roleString;
    }

    public String getRoleString() {
        return roleString;
    }
}
