package feign.refactoring;
// 1
import feign.template.QueryTemplate;

import java.util.Map;
import java.util.Optional;

// 5
public final class QueryRequestLine {
    private static final String INTERROGACAO = "?";
    private static final String E_COMERCIAL = "&";

    static Optional<String> process(final Map<String, QueryTemplate> queries){
        final Optional<String> optionalResult = queries.values()
                .stream()
                .map(QueryTemplate::toString) // 1
                .reduce((a, b) -> a + E_COMERCIAL + b); // 1

        if(!optionalResult.isPresent()) return Optional.empty(); //1

        String result = optionalResult.get();

        /* remove any trailing ampersands */
        if (result.endsWith(E_COMERCIAL)) { // 1
            result = result.substring(0, result.length() - 1);
        }
        return Optional.of(INTERROGACAO + result);
    }
}
