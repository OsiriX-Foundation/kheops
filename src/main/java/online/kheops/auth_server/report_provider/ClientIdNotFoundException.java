package online.kheops.auth_server.report_provider;

public class ClientIdNotFoundException extends Exception{

    public ClientIdNotFoundException(String message) {
        super(message);
    }

    public ClientIdNotFoundException(String message, Throwable e) {
        super(message, e);
    }

}
