package feign.refactoring;

import feign.RequestTemplate;
import feign.Util;
import feign.template.QueryTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

//9
public class QueryRequestResolver {
    private static final Pattern QUERY_STRING_PATTERN = Pattern.compile("(?<!\\{)\\?");

    static void resolver(Map<String, QueryTemplate> queries, Map<String, ?> variables, RequestTemplate resolved, StringBuilder uri){ //2
        if(queries == null || queries.isEmpty()) return ;//1
        resolved.queries(Collections.emptyMap());
        queries.values()
                .stream()
                .map(queryTemplate -> queryTemplate.expand(variables))// 1
                .filter(Util::isNotBlank)// 1
                .reduce((a, b) -> a + "&" + b) // 1
                .ifPresent(query -> {
                    final String operator = QUERY_STRING_PATTERN.matcher(uri).find() ? "&" : "?"; //2
                    uri.append(operator);
                    uri.append(query);
                });// 1
    }
}
