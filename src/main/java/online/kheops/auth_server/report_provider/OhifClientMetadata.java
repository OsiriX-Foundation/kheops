package online.kheops.auth_server.report_provider;

import java.net.URI;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import static online.kheops.auth_server.report_provider.ClientMetadataListUriParameter.REDIRECT_URIS;
import static online.kheops.auth_server.report_provider.ClientMetadataOptionalAuthMethodParameter.TOKEN_ENDPOINT_AUTH_METHOD;
import static online.kheops.auth_server.report_provider.ClientMetadataOptionalStringParameter.CLIENT_NAME;
import static online.kheops.auth_server.report_provider.ClientMetadataStringParameter.CLIENT_ID;
import static online.kheops.auth_server.report_provider.ClientMetadataURIParameter.INITIATE_LOGIN_URI;
import static online.kheops.auth_server.report_provider.ClientMetadataWithDefaults.EMPTY_DEFAULT_METADATA;
import static online.kheops.auth_server.token.TokenClientAuthenticationType.NONE;

public class OhifClientMetadata implements ClientMetadata {

    private static final ClientMetadataParameterMap OHIF_METADATA = ohifMetadata();

    private final ClientMetadataParameterMap metadata;

    public OhifClientMetadata(URI rootURI) {
        metadata = new ClientMetadataParameterMap();

        metadata.put(INITIATE_LOGIN_URI, rootURI.resolve("/login"));
        metadata.put(REDIRECT_URIS, Collections.singletonList(rootURI.resolve("/callback")));
        metadata.putAll(OHIF_METADATA);
    }

    @Override
    public <T> T getValue(ClientMetadataParameter<T> parameter) {
        if (metadata.containsKey(parameter)) {
            return metadata.get(parameter);
        } else {
            return EMPTY_DEFAULT_METADATA.getValue(parameter);
        }
    }

    @Override
    public <T> T getValue(ClientMetadataParameter<T> parameter, Locale local) {
        return getValue(parameter);
    }

    private static ClientMetadataParameterMap ohifMetadata() {
        final ClientMetadataParameterMap ohifMetadata = new ClientMetadataParameterMap();

        ohifMetadata.put(CLIENT_NAME, Optional.of("ohif"));
        ohifMetadata.put(CLIENT_ID, "ohif");
        ohifMetadata.put(TOKEN_ENDPOINT_AUTH_METHOD, Optional.of(NONE));

        return ohifMetadata;
    }
}
