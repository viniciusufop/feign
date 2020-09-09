package feign.refactoring;

import feign.RequestTemplate;
import feign.template.HeaderTemplate;
import feign.template.TemplateChunk;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

// 9
public final class HeaderRequest {
    private final Map<String, HeaderTemplate> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER); //1

    public Map<String, HeaderTemplate> getHeaders() {
        return headers;
    }
    /**
     * Add a header using the supplied Chunks.
     *
     * @param name of the header.
     * @param chunks to add.
     * @return a RequestTemplate for chaining.
     */
    //2
    public void header(String name, TemplateChunk... chunks) { // 1
        if (chunks == null) {// 1
            throw new IllegalArgumentException("chunks are required.");
        }
        AppendHeaderRequest.appendHeader(headers, name, Arrays.asList(chunks));
    }

    /**
     * Specify a Header, with the specified values. Values can be literals or template expressions.
     *
     * @param name of the header.
     * @param values for this header.
     * @return a RequestTemplate for chaining.
     */
    // 2
    public void header(String name, Iterable<String> values) {
        validateName(name);
        if (values == null) { // 1
            values = Collections.emptyList();
        }
        AppendHeaderRequest.appendHeader(headers, name, values);
    }

    /**
     * Clear on reader from {@link RequestTemplate}
     *
     * @param name of the header.
     * @return a RequestTemplate for chaining.
     */
    // 1
    public void removeHeader(String name) {
        validateName(name);
        this.headers.remove(name);
    }
    //1
    private void validateName(final String name){
        if (name == null || name.isEmpty()) { // 1
            throw new IllegalArgumentException("name is required.");
        }
    }

    /**
     * Headers for this Request.
     *
     * @param headers to use.
     * @return a RequestTemplate for chaining.
     */
    // 2
    public void headers(Map<String, Collection<String>> headers) {
        if (headers != null && !headers.isEmpty()) { // 1
            headers.forEach(this::header);
        } else { // 1
            this.headers.clear();
        }
    }
}
