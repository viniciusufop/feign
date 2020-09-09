package feign.refactoring;

import feign.RequestTemplate;
import feign.template.QueryTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
// 8
public final class QueryRequest {
    private final Map<String, QueryTemplate> queries = new LinkedHashMap<>(); //1

    public Map<String, QueryTemplate> getQueries() {
        return queries;
    }

    // 3
    public RequestTemplate queries(final RequestTemplate requestTemplate, final Map<String, Collection<String>> queries) {
        if (queries == null || queries.isEmpty()) {  // 1
            this.queries.clear();
        } else {  // 1
            queries.forEach(requestTemplate::query); // 1
        }
        return requestTemplate;
    }

    // 1
    public Map<String, Collection<String>> queries() {
        Map<String, Collection<String>> queryMap = new LinkedHashMap<>();
        this.queries.forEach((key, queryTemplate) -> {  // 1
            final List<String> values = new ArrayList<>(queryTemplate.getValues());
            /* add the expanded collection, but lock it */
            queryMap.put(key, Collections.unmodifiableList(values));
        });
        return Collections.unmodifiableMap(queryMap);
    }

    // 4
    public String queryLine() {
        final Optional<String> optionalResult = this.queries.values()
                .stream()
                .map(QueryTemplate::toString) // 1
                .reduce((a, b) -> a + "&" + b);

        if(!optionalResult.isPresent()) return ""; //1

        String result = optionalResult.get();

        /* remove any trailing ampersands */
        if (result.endsWith("&")) { // 1
            result = result.substring(0, result.length() - 1);
        }
        return "?" + result;
    }
}
