package online.kheops.auth_server.study;

public class StudyNotFoundException extends Exception{
    public StudyNotFoundException(String message) {
        super(message);
    }

    public StudyNotFoundException(String message, Throwable e) {
        super(message, e);
    }
}
