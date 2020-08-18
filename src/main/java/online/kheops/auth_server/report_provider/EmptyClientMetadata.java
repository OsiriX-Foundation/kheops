package online.kheops.auth_server.report_provider;

import java.util.Locale;

public final class EmptyClientMetadata implements ClientMetadata {

    final public static EmptyClientMetadata EMPTY_CLIENT_METADATA = new EmptyClientMetadata();

    @Override
    public <T> T getValue(ClientMetadataParameter<T> parameter) {
        return parameter.getEmptyValue();
    }

    @Override
    public <T> T getValue(ClientMetadataParameter<T> parameter, Locale local) {
        return getValue(parameter);
    }
}
