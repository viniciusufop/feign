package feign.refactoring;
// 2
import feign.CollectionFormat;
import feign.template.QueryTemplate;

import java.nio.charset.Charset;
import java.util.Map;

// 5
public final class QueryRequestAppend {
    // 3
    static void appendQuery(final Map<String, QueryTemplate> queries, final String name,
                            final Iterable<String> values, final CollectionFormat collectionFormat,
                            final Charset charset, final boolean decodeSlash) {
        if (!values.iterator().hasNext()) {  // 1
            /* empty value, clear the existing values */
            queries.remove(name);
            return;
        }

        /* create a new query template out of the information here */
        queries.compute(name, (key, queryTemplate) -> {  // 1
            if (queryTemplate == null) {  // 1
                return QueryTemplate.create(name, values, charset, collectionFormat, decodeSlash);
            } else {  // 1
                return QueryTemplate.append(queryTemplate, values, collectionFormat, decodeSlash);
            }
        });
    }
}
