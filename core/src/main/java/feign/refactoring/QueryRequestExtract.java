package feign.refactoring;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 6
public final class QueryRequestExtract {

    static Map<String, List<String>> extract(String queryString) {
        /* split the query string up into name value pairs */
        return Arrays.stream(queryString.split("&"))
                .map(QueryRequestExtract::splitQueryParameter) // 1
                .collect(Collectors.groupingBy( // 1
                        AbstractMap.SimpleImmutableEntry::getKey,
                        LinkedHashMap::new,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList()))); // 3
    }
    // 1
    private static AbstractMap.SimpleImmutableEntry<String, String> splitQueryParameter(final String pair) {
        final String[] split = pair.split("=");
        if(split.length < 2) return new AbstractMap.SimpleImmutableEntry<>(pair, null); //1
        return new AbstractMap.SimpleImmutableEntry<>(split[0], split[1]);
    }
}
