package online.kheops.auth_server;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import static online.kheops.auth_server.util.Consts.HOST_ROOT_PARAMETER;

public class KheopsInstance {

    @Context
    ServletContext context;

    public KheopsInstance() { /*empty*/ }

    public String get() { return context.getInitParameter(HOST_ROOT_PARAMETER); }
}