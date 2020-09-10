package feign.refactoring;
// 2
import feign.RequestTemplate;
import feign.template.QueryTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// 6
public class QueryUtils {

    // 3
    static void queries(final Map<String, QueryTemplate> mapQueries,
                        final RequestTemplate requestTemplate,
                        final Map<String, Collection<String>> queries) {
        if (queries == null || queries.isEmpty()) {  // 1
            mapQueries.clear();
        } else {  // 1
            queries.forEach(requestTemplate::query); // 1
        }
    }

    // 1
    static Map<String, Collection<String>> queries(final Map<String, QueryTemplate> queries) {
        Map<String, Collection<String>> queryMap = new LinkedHashMap<>();
        queries.forEach((key, queryTemplate) -> {  // 1
            final List<String> values = new ArrayList<>(queryTemplate.getValues());
            /* add the expanded collection, but lock it */
            queryMap.put(key, Collections.unmodifiableList(values));
        });
        return Collections.unmodifiableMap(queryMap);
    }

}
