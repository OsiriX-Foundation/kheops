package online.kheops.auth_server.accesstoken;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

final class AccessTokenUtils {
    private static final Pattern scopeValidator = Pattern.compile("[\\x{21}\\x{23}-\\x{5B}\\x{5D}-\\x{7E}]+");
    private static final Pattern scopeStringValidator = Pattern.compile("[\\x{20}\\x{21}\\x{23}-\\x{5B}\\x{5D}-\\x{7E}]+");
    private static final ConcurrentHashMap<String, Pattern> scopePatterns = new ConcurrentHashMap<>();

    private AccessTokenUtils() {}

    static boolean StringContainsScope(String scopeString, String scope) {
        if (!scopeValidator.matcher(scope).matches()) {
            throw new IllegalArgumentException("Scope contains illegal characters");
        }
        if (!scopeStringValidator.matcher(scopeString).matches()) {
            throw new IllegalArgumentException("Scope list contains illegal characters");
        }
        return scopePatterns.computeIfAbsent(scope, key -> Pattern.compile("\\b" + key + "\\b"))
                .matcher(scopeString)
                .find();
    }
}
