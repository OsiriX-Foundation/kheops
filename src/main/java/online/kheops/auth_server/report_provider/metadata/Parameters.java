package online.kheops.auth_server.report_provider.metadata;

import online.kheops.auth_server.report_provider.metadata.parameters.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class Parameters {

  public static final Collection<Class<? extends Parameter<?>>> PARAMETER_ENUMS =
      Collections.unmodifiableCollection(
          verifyEnum(
              List.of(
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
                  URIParameter.class)));

  public static final Collection<Parameter<?>> PARAMETERS =
      Collections.unmodifiableCollection(parameterList());

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

  private static Map<String, ? extends Parameter<?>> parameterMap() {
    final Map<String, Parameter<?>> parameterMap = new HashMap<>();

    for (Parameter<?> parameter : PARAMETERS) {
      parameterMap.put(parameter.getKey(), parameter);
    }

    return parameterMap;
  }

  private static final int LANGUAGE_DELIMITER = '#';

  private static List<Class<? extends Parameter<?>>> verifyEnum(
      List<Class<? extends Parameter<?>>> enumList) {
    for (Class<? extends Parameter<?>> enumClass : enumList) {
      if (!Enum.class.isAssignableFrom(enumClass)) {
        throw new IllegalArgumentException(
            "class " + enumClass.getCanonicalName() + " is not an Enum type");
      }
    }

    return enumList;
  }

  private static List<Parameter<?>> parameterList() {

    final List<Parameter<?>> fullParameterList = new ArrayList<>();
    try {
      for (Class<? extends Parameter<?>> parameterEnum : PARAMETER_ENUMS) {
        final Parameter<?>[] parameterList =
            (Parameter<?>[]) parameterEnum.getDeclaredMethod("values").invoke(null);
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
