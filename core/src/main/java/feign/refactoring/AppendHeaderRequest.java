package feign.refactoring;

import feign.template.HeaderTemplate;
import feign.template.TemplateChunk;

import java.util.Collections;
import java.util.List;
import java.util.Map;

//9
public final class AppendHeaderRequest {

    /**
     * Create a Header Template.
     *
     * @param name of the header
     * @param values for the header, may be expressions.
     * @return a RequestTemplate for chaining.
     */

    // 3
    public static void appendHeader(final Map<String, HeaderTemplate> headers, final String name, final Iterable<String> values) {
        if (!values.iterator().hasNext()) { // 1
            /* empty value, clear the existing values */
            headers.remove(name);
        }
        if (name.equals("Content-Type")) { // 1
            // a client can only produce content of one single type, so always override Content-Type and
            // only add a single type
            headers.remove(name);
            headers.put(name,
                    HeaderTemplate.create(name, Collections.singletonList(values.iterator().next())));
        }
        headers.compute(name, (headerName, headerTemplate) -> returnHeaderTemplate(headerTemplate, headerName, values)); // 1
    }

    //2
    private static HeaderTemplate returnHeaderTemplate(final HeaderTemplate headerTemplate, final String name, final Iterable<String> values){
        if (headerTemplate == null) { // 1
            return HeaderTemplate.create(name, values);
        } else { // 1
            return HeaderTemplate.append(headerTemplate, values);
        }
    }

    // 2
    public static void appendHeader(final Map<String, HeaderTemplate> headers, final String name, final List<TemplateChunk> chunks) {
        if (chunks.isEmpty()) { // 1
            headers.remove(name);
        }
        headers.compute(name, (headerName, headerTemplate) -> returnHeaderTemplate(headerTemplate, name, chunks)); // 1
    }

    // 2
    private static HeaderTemplate returnHeaderTemplate(final HeaderTemplate headerTemplate, final String name, final List<TemplateChunk> chunks){
        if (headerTemplate == null) { // 1
            return HeaderTemplate.from(name, chunks);
        } else { // 1
            return HeaderTemplate.appendFrom(headerTemplate, chunks);
        }
    }
}
