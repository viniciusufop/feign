package feign.refactoring;
// 3
import feign.CollectionFormat;
import feign.RequestTemplate;
import feign.template.QueryTemplate;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// 13
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
    // 1
    public void appendQuery(String name,Iterable<String> values,CollectionFormat collectionFormat,
                            Charset charset, boolean decodeSlash) {
        QueryRequestAppend.appendQuery(queries, name, values, collectionFormat, charset, decodeSlash); // 1
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
    // 1
    public void queries(final RequestTemplate requestTemplate, final Map<String, Collection<String>> queries) {
        QueryUtils.queries(this.queries, requestTemplate, queries);
    }

    public Map<String, Collection<String>> queries() {
        return QueryUtils.queries(queries);
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
