package feign.refactoring;

import feign.CollectionFormat;
import feign.RequestTemplate;
import feign.template.QueryTemplate;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
// 23
public final class QueryRequest {
    private final Map<String, QueryTemplate> queries = new LinkedHashMap<>(); //1
    // 1
    public void addAll(final QueryRequest queryRequest){
        if (!queryRequest.queries.isEmpty()) { //1
            queries.putAll(queryRequest.queries);
        }
    }

    // 3
    public List<String> variables(){
        return queries.values()
                .stream()
                .map(QueryTemplate::getVariables)// 1
                .reduce((a, b) -> {a.addAll(b); return a;})// 1
                .orElse(new ArrayList<>());// 1
    }
    // 4
    public void appendQuery(String name,Iterable<String> values,CollectionFormat collectionFormat,
                            Charset charset, boolean decodeSlash) {
        if (!values.iterator().hasNext()) {  // 1
            /* empty value, clear the existing values */
            this.queries.remove(name);
            return;
        }

        /* create a new query template out of the information here */
        this.queries.compute(name, (key, queryTemplate) -> {  // 1
            if (queryTemplate == null) {  // 1
                return QueryTemplate.create(name, values, charset, collectionFormat, decodeSlash);
            } else {  // 1
                return QueryTemplate.append(queryTemplate, values, collectionFormat, decodeSlash);
            }
        });
    }

    //4
    public void extractQueryTemplates(RequestTemplate requestTemplate, String queryString, boolean append) { //1
        /* split the query string up into name value pairs */
        Map<String, List<String>> queryParameters = ExtractQueryTemplate.extract(queryString); // 1

        /* add them to this template */
        if (!append) { // 1
            /* clear the queries and use the new ones */
            this.queries.clear();
        }
        queryParameters.forEach(requestTemplate::query); // 1
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
    public Optional<String> queryLine() {
        final Optional<String> optionalResult = this.queries.values()
                .stream()
                .map(QueryTemplate::toString) // 1
                .reduce((a, b) -> a + "&" + b);

        if(!optionalResult.isPresent()) return Optional.empty(); //1

        String result = optionalResult.get();

        /* remove any trailing ampersands */
        if (result.endsWith("&")) { // 1
            result = result.substring(0, result.length() - 1);
        }
        return Optional.of("?" + result);
    }
    // 1
    public void resolver(Map<String, ?> variables, RequestTemplate resolved, StringBuilder uri) {
        QueryResolver.resolver(queries, variables, resolved, uri); // 1
    }
    // 2
    public void decodeSlash(boolean decodeSlash, Charset charset, CollectionFormat collectionFormat) { //1
        if (!queries.isEmpty()) { // 1
            queries.replaceAll((key, queryTemplate) -> QueryTemplate.create(
                    /* replace the current template with new ones honoring the decode value */
                    queryTemplate.getName(), queryTemplate.getValues(), charset, collectionFormat,
                    decodeSlash));

        }
    }
}
