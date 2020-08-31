package online.kheops.auth_server.report_provider.metadata;

import java.util.List;
import java.util.Locale;

public final class EmptyOidcMetadata implements OidcMetadata {

    final public static EmptyOidcMetadata EMPTY_OIDC_METADATA = new EmptyOidcMetadata();

    @Override
    public <T> T getValue(Parameter<? extends T> parameter) {
        return parameter.getEmptyValue();
    }

    @Override
    public <T> T getValue(Parameter<? extends T> parameter, List<Locale.LanguageRange> priorityList) {
        return getValue(parameter);
    }
}
