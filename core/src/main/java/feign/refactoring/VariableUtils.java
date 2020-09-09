package feign.refactoring;
// 2
import feign.template.HeaderTemplate;
import feign.template.QueryTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
// 6
public final class VariableUtils {
    // 1
    static List<String> extractQueryVariables(final Map<String, QueryTemplate> queries){
        return reduce(queries.values()
                .stream()
                .map(QueryTemplate::getVariables));// 1
    }
    // 1
    static List<String> extractHeaderVariables(final Map<String, HeaderTemplate> headers){
        return reduce(headers.values()
                .stream()
                .map(HeaderTemplate::getVariables));// 1
    }
    // 2
    private static List<String> reduce(Stream<List<String>> streams){
        return streams.reduce((a, b) -> {a.addAll(b); return a;})// 1
                .orElse(new ArrayList<>());// 1
    }
}
