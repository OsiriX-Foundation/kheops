package online.kheops.auth_server.report_provider;

import online.kheops.auth_server.report_provider.metadata.OidcMetadata;
import online.kheops.auth_server.report_provider.metadata.Parameter;
import online.kheops.auth_server.report_provider.metadata.ParameterHashMap;
import online.kheops.auth_server.report_provider.metadata.ParameterMap;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static online.kheops.auth_server.report_provider.metadata.parameters.ListUriParameter.REDIRECT_URIS;
import static online.kheops.auth_server.report_provider.metadata.parameters.OptionalAuthMethodParameter.TOKEN_ENDPOINT_AUTH_METHOD;
import static online.kheops.auth_server.report_provider.metadata.parameters.OptionalStringParameter.CLIENT_NAME;
import static online.kheops.auth_server.report_provider.metadata.parameters.OptionalUriBuilderParameter.KHEOPS_TARGET_LINK_URI_TEMPLATE;
import static online.kheops.auth_server.report_provider.metadata.parameters.StringParameter.CLIENT_ID;
import static online.kheops.auth_server.report_provider.metadata.parameters.URIParameter.INITIATE_LOGIN_URI;
import static online.kheops.auth_server.report_provider.metadata.ClientMetadataWithDefaults.EMPTY_DEFAULT_METADATA;
import static online.kheops.auth_server.token.TokenClientAuthenticationType.NONE;

public class OhifClientMetadata implements OidcMetadata {

    private static final ParameterMap OHIF_METADATA = ohifMetadata();

    private final ParameterMap metadata;

    public OhifClientMetadata(URI rootURI) {
        metadata = new ParameterHashMap();

        metadata.put(INITIATE_LOGIN_URI, rootURI.resolve("/login"));
        metadata.put(REDIRECT_URIS, Collections.singletonList(rootURI.resolve("/callback")));
        metadata.put(KHEOPS_TARGET_LINK_URI_TEMPLATE, Optional.of(UriBuilder.fromUri(rootURI).path("/viewer/{StudyInstanceUID}")));
        metadata.putAll(OHIF_METADATA);
    }

    @Override
    public <T> T getValue(Parameter<? extends T> parameter) {
        if (metadata.containsKey(parameter)) {
            return metadata.get(parameter);
        } else {
            return EMPTY_DEFAULT_METADATA.getValue(parameter);
        }
    }

    @Override
    public <T> T getValue(Parameter<? extends T> parameter, List<Locale.LanguageRange> priorityList) {
        return getValue(parameter);
    }

    private static ParameterMap ohifMetadata() {
        final ParameterMap ohifMetadata = new ParameterHashMap();

        ohifMetadata.put(CLIENT_NAME, Optional.of("ohif"));
        ohifMetadata.put(CLIENT_ID, "ohif");
        ohifMetadata.put(TOKEN_ENDPOINT_AUTH_METHOD, Optional.of(NONE));

        return ohifMetadata;
    }
}
