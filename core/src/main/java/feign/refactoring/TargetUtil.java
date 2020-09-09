package feign.refactoring;

import feign.Util;
import feign.template.UriUtils;

import java.net.URI;
import java.util.Optional;

// 5
public final class TargetUtil {
    public static Optional<URI> generateURI(final String value) throws IllegalArgumentException {
        /* target can be empty */
        if (Util.isBlank(value)) return Optional.empty(); // 1

        /* verify that the target contains the scheme, host and port */
        if (!UriUtils.isAbsolute(value)) //1
            throw new IllegalArgumentException("target values must be absolute.");

        final String target = removeLastChar(value);
        try { // 1
            /* parse the target */
            return Optional.of(URI.create(target));
        } catch (IllegalArgumentException iae) { // 1
            /* the uri provided is not a valid one, we can't continue */
            throw new IllegalArgumentException("Target is not a valid URI.", iae);
        }

    }

    private static String removeLastChar(final String target){
        if (target.endsWith("/"))  return target.substring(0, target.length() - 1); // 1
        return target;
    }
}
