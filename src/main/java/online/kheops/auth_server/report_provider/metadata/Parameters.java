package online.kheops.auth_server.report_provider.metadata;

import online.kheops.auth_server.report_provider.metadata.parameters.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static online.kheops.auth_server.report_provider.metadata.parameters.ListAlgorithmParameter.*;
import static online.kheops.auth_server.report_provider.metadata.parameters.ListAuthMethodParameter.TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED;
import static online.kheops.auth_server.report_provider.metadata.parameters.ListClaimTypeParameter.CLAIM_TYPES_SUPPORTED;
import static online.kheops.auth_server.report_provider.metadata.parameters.ListDisplayTypeParameter.DISPLAY_VALUES_SUPPORTED;
import static online.kheops.auth_server.report_provider.metadata.parameters.ListEncodingParameter.*;
import static online.kheops.auth_server.report_provider.metadata.parameters.ListGrantTypeParameter.GRANT_TYPES;
import static online.kheops.auth_server.report_provider.metadata.parameters.ListGrantTypeParameter.GRANT_TYPES_SUPPORTED;
import static online.kheops.auth_server.report_provider.metadata.parameters.ListJwkParameter.JWKS;
import static online.kheops.auth_server.report_provider.metadata.parameters.ListLocalesParameter.CLAIMS_LOCALES_SUPPORTED;
import static online.kheops.auth_server.report_provider.metadata.parameters.ListResponseModeParameter.RESPONSE_MODES_SUPPORTED;
import static online.kheops.auth_server.report_provider.metadata.parameters.ListResponseTypeParameter.RESPONSE_TYPES;
import static online.kheops.auth_server.report_provider.metadata.parameters.ListResponseTypeParameter.RESPONSE_TYPES_SUPPORTED;
import static online.kheops.auth_server.report_provider.metadata.parameters.ListStringParameter.*;
import static online.kheops.auth_server.report_provider.metadata.parameters.ListSubjectTypeParameter.SUBJECT_TYPES_SUPPORTED;
import static online.kheops.auth_server.report_provider.metadata.parameters.ListUriParameter.*;
import static online.kheops.auth_server.report_provider.metadata.parameters.OptionalAlgorithmParameter.*;
import static online.kheops.auth_server.report_provider.metadata.parameters.OptionalApplicationTypeParameter.APPLICATION_TYPE;
import static online.kheops.auth_server.report_provider.metadata.parameters.OptionalBooleanParameter.*;
import static online.kheops.auth_server.report_provider.metadata.parameters.OptionalEncodingParameter.*;
import static online.kheops.auth_server.report_provider.metadata.parameters.OptionalIntParameter.DEFAULT_MAX_AGE;
import static online.kheops.auth_server.report_provider.metadata.parameters.OptionalStringParameter.CLIENT_NAME;
import static online.kheops.auth_server.report_provider.metadata.parameters.OptionalSubjectTypeParameter.SUBJECT_TYPE;
import static online.kheops.auth_server.report_provider.metadata.parameters.OptionalUriParameter.*;
import static online.kheops.auth_server.report_provider.metadata.parameters.URIParameter.*;

public final class Parameters {

  public static final Set<Class<? extends Parameter<?>>> PARAMETER_ENUMS =
      Collections.unmodifiableSet(parameterEnums());

  public static final Set<Parameter<?>> PARAMETERS = Collections.unmodifiableSet(parameterSet());

  public static final Set<Parameter<?>> CLIENT_REGISTRATION_PARAMETERS =
      Collections.unmodifiableSet(clientRegistrationParameters());

  public static final Set<Parameter<?>> PROVIDER_PARAMETERS =
      Collections.unmodifiableSet(providerParameters());

  public static Parameter<?> parameterFromKey(String key) {
    final int languageDelimiterIndex = key.indexOf(LANGUAGE_DELIMITER);
    final boolean hasLanguageDelimiter = languageDelimiterIndex != -1;

    final String stripedKey;
    if (hasLanguageDelimiter) {
      stripedKey = key.substring(languageDelimiterIndex);
    } else {
      stripedKey = key;
    }

    return PARAMETER_MAP.get(stripedKey);
  }

  private static final Map<String, ? extends Parameter<?>> PARAMETER_MAP =
      Collections.unmodifiableMap(parameterMap());

  private static Set<Class<? extends Parameter<?>>> parameterEnums() {
    return verifyEnum(
        Set.of(
            ListAlgorithmParameter.class,
            ListAuthMethodParameter.class,
            ListClaimTypeParameter.class,
            ListDisplayTypeParameter.class,
            ListEncodingParameter.class,
            OptionalAlgorithmParameter.class,
            OptionalAuthMethodParameter.class,
            OptionalEncodingParameter.class,
            ListGrantTypeParameter.class,
            ListJwkParameter.class,
            ListLocalesParameter.class,
            ListResponseModeParameter.class,
            ListResponseTypeParameter.class,
            ListStringParameter.class,
            ListSubjectTypeParameter.class,
            ListUriParameter.class,
            OptionalApplicationTypeParameter.class,
            OptionalBooleanParameter.class,
            OptionalIntParameter.class,
            OptionalStringParameter.class,
            OptionalSubjectTypeParameter.class,
            OptionalUriBuilderParameter.class,
            OptionalUriParameter.class,
            StringParameter.class,
            URIParameter.class));
  }

  private static Set<Parameter<?>> clientRegistrationParameters() {
    return Set.of(
        REDIRECT_URIS,
        RESPONSE_TYPES,
        GRANT_TYPES,
        APPLICATION_TYPE,
        CONTACTS,
        CLIENT_NAME,
        LOGO_URI,
        CLIENT_URI,
        POLICY_URI,
        TOS_URI,
        JWKS_URI,
        JWKS,
        SECTOR_IDENTIFIER_URI,
        SUBJECT_TYPE,
        ID_TOKEN_SIGNED_RESPONSE_ALG,
        ID_TOKEN_ENCRYPTED_RESPONSE_ALG,
        ID_TOKEN_ENCRYPTED_RESPONSE_ENC,
        USERINFO_SIGNED_RESPONSE_ALG,
        USERINFO_ENCRYPTED_RESPONSE_ALG,
        USERINFO_ENCRYPTED_RESPONSE_ENC,
        REQUEST_OBJECT_SIGNING_ALG,
        REQUEST_OBJECT_ENCRYPTION_ALG,
        REQUEST_OBJECT_ENCRYPTION_ENC,
        TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED,
        TOKEN_ENDPOINT_AUTH_SIGNING_ALG,
        DEFAULT_MAX_AGE,
        REQUIRE_AUTH_TIME,
        DEFAULT_ACR_VALUES,
        INITIATE_LOGIN_URI,
        REQUEST_URIS,
        POST_LOGOUT_REDIRECT_URIS);
  }

  private static Set<Parameter<?>> providerParameters() {
    return Set.of(
        ISSUER,
        AUTHORIZATION_ENDPOINT,
        TOKEN_ENDPOINT,
        USERINFO_ENDPOINT,
        JWKS_URI,
        REGISTRATION_ENDPOINT,
        SCOPES_SUPPORTED,
        RESPONSE_TYPES_SUPPORTED,
        RESPONSE_MODES_SUPPORTED,
        GRANT_TYPES_SUPPORTED,
        ACR_VALUES_SUPPORTED,
        SUBJECT_TYPES_SUPPORTED,
        ID_TOKEN_SIGNING_ALG_VALUES_SUPPORTED,
        ID_TOKEN_ENCRYPTION_ALG_VALUES_SUPPORTED,
        ID_TOKEN_ENCRYPTION_ENC_VALUES_SUPPORTED,
        USERINFO_SIGNING_ALG_VALUES_SUPPORTED,
        USERINFO_ENCRYPTION_ALG_VALUES_SUPPORTED,
        USERINFO_ENCRYPTION_ENC_VALUES_SUPPORTED,
        REQUEST_OBJECT_SIGNING_ALG_VALUES_SUPPORTED,
        REQUEST_OBJECT_ENCRYPTION_ALG_VALUES_SUPPORTED,
        REQUEST_OBJECT_ENCRYPTION_ENC_VALUES_SUPPORTED,
        TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED,
        TOKEN_ENDPOINT_AUTH_SIGNING_ALG_VALUES_SUPPORTED,
        DISPLAY_VALUES_SUPPORTED,
        CLAIM_TYPES_SUPPORTED,
        CLAIMS_SUPPORTED,
        SERVICE_DOCUMENTATION,
        CLAIMS_LOCALES_SUPPORTED,
        REQUEST_PARAMETER_SUPPORTED,
        REQUEST_URI_PARAMETER_SUPPORTED,
        REQUIRE_REQUEST_URI_REGISTRATION,
        OP_POLICY_URI,
        OP_TOS_URI);
  }

  private static Set<Class<? extends Parameter<?>>> verifyEnum(
      Set<Class<? extends Parameter<?>>> enumList) {
    for (Class<? extends Parameter<?>> enumClass : enumList) {
      if (!Enum.class.isAssignableFrom(enumClass)) {
        throw new IllegalArgumentException(
            "class " + enumClass.getCanonicalName() + " is not an Enum type");
      }
    }

    return enumList;
  }

  private static Map<String, ? extends Parameter<?>> parameterMap() {
    final Map<String, Parameter<?>> parameterMap = new HashMap<>();

    for (Parameter<?> parameter : PARAMETERS) {
      parameterMap.put(parameter.getKey(), parameter);
    }

    return parameterMap;
  }

  private static final int LANGUAGE_DELIMITER = '#';

  private static Set<Parameter<?>> parameterSet() {

    final Set<Parameter<?>> fullParameterSet = new HashSet<>();
    try {
      for (Class<? extends Parameter<?>> parameterEnum : PARAMETER_ENUMS) {
        final Parameter<?>[] parameterList =
            (Parameter<?>[]) parameterEnum.getDeclaredMethod("values").invoke(null);
        fullParameterSet.addAll(Arrays.asList(parameterList));
      }
    } catch (NoSuchMethodException
        | IllegalAccessException
        | InvocationTargetException
        | ClassCastException e) {
      throw new IllegalArgumentException(e);
    }

    return fullParameterSet;
  }
}
