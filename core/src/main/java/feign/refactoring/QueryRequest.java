package feign.refactoring;
// 3
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

// 19
public final class QueryRequest {
    private final Map<String, QueryTemplate> queries = new LinkedHashMap<>();
    // 1
    public void addAll(final QueryRequest queryRequest){
        if (!queryRequest.queries.isEmpty()) { //1
            queries.putAll(queryRequest.queries);
        }
    }

    // 1
    public List<String> variables(){
        return VariableUtils.extractQueryVariables(queries); // 1
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

    //3
    public void extractQueryTemplates(RequestTemplate requestTemplate, String queryString, boolean append) {
        /* split the query string up into name value pairs */
        Map<String, List<String>> queryParameters = QueryRequestExtract.extract(queryString); // 1

        /* add them to this template */
        if (!append) { // 1
            /* clear the queries and use the new ones */
            this.queries.clear();
        }
        queryParameters.forEach(requestTemplate::query); // 1
    }

    // 3
    public void queries(final RequestTemplate requestTemplate, final Map<String, Collection<String>> queries) {
        if (queries == null || queries.isEmpty()) {  // 1
            this.queries.clear();
        } else {  // 1
            queries.forEach(requestTemplate::query); // 1
        }
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
    // 1
    public Optional<String> queryLine() {
        return QueryRequestLine.process(queries); // 1
    }

    public void resolver(Map<String, ?> variables, RequestTemplate resolved, StringBuilder uri) {
        QueryRequestResolver.resolver(queries, variables, resolved, uri);
    }
    // 2
    public void decodeSlash(boolean decodeSlash, Charset charset, CollectionFormat collectionFormat) {
        if (!queries.isEmpty()) { // 1
            queries.replaceAll((key, queryTemplate) -> QueryTemplate.create(
                    /* replace the current template with new ones honoring the decode value */
                    queryTemplate.getName(), queryTemplate.getValues(), charset, collectionFormat,
                    decodeSlash)); //1

        }
    }
}
