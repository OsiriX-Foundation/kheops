package online.kheops.auth_server.report_provider;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class ClientMetadataParameters {

  public static final Collection<Class<? extends ClientMetadataParameter<?>>> PARAMETER_ENUMS =
      Collections.unmodifiableCollection(
          verifyEnum(
              List.of(
                  ClientMetadataOptionalAlgorithmParameter.class,
                  ClientMetadataOptionalAuthMethodParameter.class,
                  ClientMetadataOptionalEncodingParameter.class,
                  ClientMetadataListGrantTypeParameter.class,
                  ClientMetadataListJwkParameter.class,
                  ClientMetadataListResponseTypeParameter.class,
                  ClientMetadataListStringParameter.class,
                  ClientMetadataListUriParameter.class,
                  ClientMetadataOptionalApplicationTypeParameter.class,
                  ClientMetadataOptionalBooleanParameter.class,
                  ClientMetadataOptionalIntParameter.class,
                  ClientMetadataOptionalStringParameter.class,
                  ClientMetadataOptionalSubjectTypeParameter.class,
                  ClientMetadataOptionalUriBuilderParameter.class,
                  ClientMetadataOptionalUriParameter.class,
                  ClientMetadataStringParameter.class,
                  ClientMetadataURIParameter.class)));

  public static final Collection<ClientMetadataParameter<?>> PARAMETERS =
      Collections.unmodifiableCollection(parameterList());

  public static ClientMetadataParameter<?> parameterFromKey(String key) {
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

  private static final Map<String, ? extends ClientMetadataParameter<?>> PARAMETER_MAP =
      Collections.unmodifiableMap(parameterMap());

  private static Map<String, ? extends ClientMetadataParameter<?>> parameterMap() {
    final Map<String, ClientMetadataParameter<?>> parameterMap = new HashMap<>();

    for (ClientMetadataParameter<?> parameter : PARAMETERS) {
      parameterMap.put(parameter.getKey(), parameter);
    }

    return parameterMap;
  }

  private static final int LANGUAGE_DELIMITER = '#';

  private static List<Class<? extends ClientMetadataParameter<?>>> verifyEnum(
      List<Class<? extends ClientMetadataParameter<?>>> enumList) {
    for (Class<? extends ClientMetadataParameter<?>> enumClass : enumList) {
      if (!Enum.class.isAssignableFrom(enumClass)) {
        throw new IllegalArgumentException(
            "class " + enumClass.getCanonicalName() + " is not an Enum type");
      }
    }

    return enumList;
  }

  private static List<ClientMetadataParameter<?>> parameterList() {

    final List<ClientMetadataParameter<?>> fullParameterList = new ArrayList<>();
    try {
      for (Class<? extends ClientMetadataParameter<?>> parameterEnum : PARAMETER_ENUMS) {
        final ClientMetadataParameter<?>[] parameterList =
            (ClientMetadataParameter<?>[]) parameterEnum.getDeclaredMethod("values").invoke(null);
        fullParameterList.addAll(Arrays.asList(parameterList));
      }
    } catch (NoSuchMethodException
        | IllegalAccessException
        | InvocationTargetException
        | ClassCastException e) {
      throw new IllegalArgumentException(e);
    }

    return fullParameterList;
  }
}
