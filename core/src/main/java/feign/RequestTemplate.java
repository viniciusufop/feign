/**
 * Copyright 2012-2020 The Feign Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package feign;

import feign.Request.HttpMethod;
import feign.refactoring.ExtractQueryTemplate;
import feign.refactoring.HeaderRequest;
import feign.refactoring.HeaderResolver;
import feign.refactoring.QueryRequest;
import feign.refactoring.QueryResolver;
import feign.refactoring.TargetUtil;
import feign.refactoring.UriInitial;
import feign.template.*;

import java.io.Serializable;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static feign.Util.*;

/**
 * Request Builder for an HTTP Target.
 * <p>
 * This class is a variation on a UriTemplate, where, in addition to the uri, Headers and Query
 * information also support template expressions.
 * </p>
 */
@SuppressWarnings("UnusedReturnValue")

//92
//78 - reducao de 14
// 70 - reducao de 8 com classe QueryRequest
// 62 - reducao de 8 com a classe HeaderRequest
public final class RequestTemplate implements Serializable {
  //9
  private static final Pattern QUERY_STRING_PATTERN = Pattern.compile("(?<!\\{)\\?");
  private final QueryRequest queryRequest = new QueryRequest(); // 1
  private final HeaderRequest headerRequest = new HeaderRequest();//1
  private String target;
  private String fragment;
  private boolean resolved = false;
  private UriTemplate uriTemplate; //1
  private BodyTemplate bodyTemplate; //1
  private HttpMethod method; //1
  private transient Charset charset = Util.UTF_8;
  private Request.Body body = Request.Body.empty();  //1
  private boolean decodeSlash = true;
  private CollectionFormat collectionFormat = CollectionFormat.EXPLODED; //1
  private MethodMetadata methodMetadata; //1
  private Target<?> feignTarget; //1

  /**
   * Create a new Request Template.
   */
  public RequestTemplate() {
    super();
  }

  /**
   * Create a new Request Template.
   *
   * @param fragment part of the request uri.
   * @param target for the template.
   * @param uriTemplate for the template.
   * @param bodyTemplate for the template, may be {@literal null}
   * @param method of the request.
   * @param charset for the request.
   * @param body of the request, may be {@literal null}
   * @param decodeSlash if the request uri should encode slash characters.
   * @param collectionFormat when expanding collection based variables.
   * @param feignTarget this template is targeted for.
   * @param methodMetadata containing a reference to the method this template is built from.
   */
  //2
  private RequestTemplate(String target,
      String fragment,
      UriTemplate uriTemplate,
      BodyTemplate bodyTemplate,
      HttpMethod method,
      Charset charset,
      Request.Body body,
      boolean decodeSlash,
      CollectionFormat collectionFormat,
      MethodMetadata methodMetadata,
      Target<?> feignTarget) {
    this.target = target;
    this.fragment = fragment;
    this.uriTemplate = uriTemplate;
    this.bodyTemplate = bodyTemplate;
    this.method = method;
    this.charset = charset;
    this.body = body;
    this.decodeSlash = decodeSlash;
    this.collectionFormat =
        (collectionFormat != null) ? collectionFormat : CollectionFormat.EXPLODED; //2
    this.methodMetadata = methodMetadata;
    this.feignTarget = feignTarget;
  }

  /**
   * Create a Request Template from an existing Request Template.
   *
   * @param requestTemplate to copy from.
   * @return a new Request Template.
   */
  //2
  public static RequestTemplate from(RequestTemplate requestTemplate) {
    RequestTemplate template =
        new RequestTemplate(
            requestTemplate.target,
            requestTemplate.fragment,
            requestTemplate.uriTemplate,
            requestTemplate.bodyTemplate,
            requestTemplate.method,
            requestTemplate.charset,
            requestTemplate.body,
            requestTemplate.decodeSlash,
            requestTemplate.collectionFormat,
            requestTemplate.methodMetadata,
            requestTemplate.feignTarget);

    if (!requestTemplate.queries().isEmpty()) { //1
      template.queryRequest.getQueries().putAll(requestTemplate.queryRequest.getQueries());
    }

    if (!requestTemplate.headers().isEmpty()) { //1
      template.headerRequest.getHeaders().putAll(requestTemplate.headerRequest.getHeaders());
    }
    return template;
  }

  /**
   * Create a Request Template from an existing Request Template.
   *
   * @param toCopy template.
   * @deprecated replaced by {@link RequestTemplate#from(RequestTemplate)}
   */
  @Deprecated
  //2
  public RequestTemplate(RequestTemplate toCopy) {
    checkNotNull(toCopy, "toCopy");
    this.target = toCopy.target;
    this.fragment = toCopy.fragment;
    this.method = toCopy.method;
    this.queryRequest.getQueries().putAll(toCopy.queryRequest.getQueries());
    this.headerRequest.getHeaders().putAll(toCopy.headerRequest.getHeaders());
    this.charset = toCopy.charset;
    this.body = toCopy.body;
    this.decodeSlash = toCopy.decodeSlash;
    this.collectionFormat =
        (toCopy.collectionFormat != null) ? toCopy.collectionFormat : CollectionFormat.EXPLODED; //2
    this.uriTemplate = toCopy.uriTemplate;
    this.bodyTemplate = toCopy.bodyTemplate;
    this.resolved = false;
    this.methodMetadata = toCopy.methodMetadata;
    this.target = toCopy.target;
    this.feignTarget = toCopy.feignTarget;
  }

  /**
   * Resolve all expressions using the variable value substitutions provided. Variable values will
   * be pct-encoded, if they are not already.
   *
   * @param variables containing the variable values to use when resolving expressions.
   * @return a new Request Template with all of the variables resolved.
   */
  //5
  public RequestTemplate resolve(Map<String, ?> variables) {

    StringBuilder uri = new StringBuilder();

    /* create a new template form this one, but explicitly */
    RequestTemplate resolved = RequestTemplate.from(this);

    if (this.uriTemplate == null) { //1
      /* create a new uri template using the default root */
      this.uriTemplate = UriTemplate.create("", !this.decodeSlash, this.charset);
    }

    String expanded = this.uriTemplate.expand(variables);
    if (expanded != null) { //1
      uri.append(expanded);
    }

    /*
     * for simplicity, combine the queries into the uri and use the resulting uri to seed the
     * resolved template.
     */
    QueryResolver.resolver(this.queryRequest.getQueries(), variables, resolved, uri); // 1

    /* add the uri to result */
    resolved.uri(uri.toString());

    /* headers */
    HeaderResolver.resolver(this.headerRequest.getHeaders(), variables, resolved); // 1

    if (this.bodyTemplate != null) { //1
      resolved.body(this.bodyTemplate.expand(variables));
    }

    /* mark the new template resolved */
    resolved.resolved = true;
    return resolved;
  }

  /**
   * Resolves all expressions, using the variables provided. Values not present in the {@code
   * alreadyEncoded} map are pct-encoded.
   *
   * @param unencoded variable values to substitute.
   * @param alreadyEncoded variable names.
   * @return a resolved Request Template
   * @deprecated use {@link RequestTemplate#resolve(Map)}. Values already encoded are recognized as
   *             such and skipped.
   */
  @SuppressWarnings("unused")
  @Deprecated
  RequestTemplate resolve(Map<String, ?> unencoded, Map<String, Boolean> alreadyEncoded) {
    return this.resolve(unencoded);
  }

  /**
   * Creates a {@link Request} from this template. The template must be resolved before calling this
   * method, or an {@link IllegalStateException} will be thrown.
   *
   * @return a new Request instance.
   * @throws IllegalStateException if this template has not been resolved.
   */
  //1
  public Request request() {
    if (!this.resolved) { // 1
      throw new IllegalStateException("template has not been resolved.");
    }
    return Request.create(this.method, this.url(), this.headers(), this.body, this);
  }

  /**
   * Set the Http Method.
   *
   * @param method to use.
   * @return a RequestTemplate for chaining.
   * @deprecated see {@link RequestTemplate#method(HttpMethod)}
   */
  //2
  @Deprecated
  public RequestTemplate method(String method) {
    checkNotNull(method, "method");
    try { // 1
      this.method = HttpMethod.valueOf(method);
    } catch (IllegalArgumentException iae) { //1
      throw new IllegalArgumentException("Invalid HTTP Method: " + method);
    }
    return this;
  }

  /**
   * Set the Http Method.
   *
   * @param method to use.
   * @return a RequestTemplate for chaining.
   */
  public RequestTemplate method(HttpMethod method) {
    checkNotNull(method, "method");
    this.method = method;
    return this;
  }

  /**
   * The Request Http Method.
   *
   * @return Http Method.
   */
  public String method() {
    return (method != null) ? method.name() : null;
  }

  /**
   * Set whether do encode slash {@literal /} characters when resolving this template.
   *
   * @param decodeSlash if slash literals should not be encoded.
   * @return a RequestTemplate for chaining.
   */
  // 1
  public RequestTemplate decodeSlash(boolean decodeSlash) {
    this.decodeSlash = decodeSlash;
    this.uriTemplate =
        UriTemplate.create(this.uriTemplate.toString(), !this.decodeSlash, this.charset);
    if (!this.queryRequest.getQueries().isEmpty()) { // 1
      this.queryRequest.getQueries().replaceAll((key, queryTemplate) -> QueryTemplate.create(
          /* replace the current template with new ones honoring the decode value */
          queryTemplate.getName(), queryTemplate.getValues(), charset, collectionFormat,
          decodeSlash));

    }
    return this;
  }

  /**
   * If slash {@literal /} characters are not encoded when resolving.
   *
   * @return true if slash literals are not encoded, false otherwise.
   */
  public boolean decodeSlash() {
    return decodeSlash;
  }

  /**
   * The Collection Format to use when resolving variables that represent {@link Iterable}s or
   * {@link Collection}s
   *
   * @param collectionFormat to use.
   * @return a RequestTemplate for chaining.
   */
  public RequestTemplate collectionFormat(CollectionFormat collectionFormat) {
    this.collectionFormat = collectionFormat;
    return this;
  }

  /**
   * The Collection Format that will be used when resolving {@link Iterable} and {@link Collection}
   * variables.
   *
   * @return the collection format set
   */
  @SuppressWarnings("unused")
  public CollectionFormat collectionFormat() {
    return collectionFormat;
  }

  /**
   * Append the value to the template.
   * <p>
   * This method is poorly named and is used primarily to store the relative uri for the request. It
   * has been replaced by {@link RequestTemplate#uri(String)} and will be removed in a future
   * release.
   * </p>
   *
   * @param value to append.
   * @return a RequestTemplate for chaining.
   * @deprecated see {@link RequestTemplate#uri(String, boolean)}
   */
  // 1
  @Deprecated
  public RequestTemplate append(CharSequence value) {
    /* proxy to url */
    if (this.uriTemplate != null) { // 1
      return this.uri(value.toString(), true);
    }
    return this.uri(value.toString());
  }

  /**
   * Insert the value at the specified point in the template uri.
   * <p>
   * This method is poorly named has undocumented behavior. When the value contains a fully
   * qualified http request url, the value is always inserted at the beginning of the uri.
   * </p>
   * <p>
   * Due to this, use of this method is not recommended and remains for backward compatibility. It
   * has been replaced by {@link RequestTemplate#target(String)} and will be removed in a future
   * release.
   * </p>
   *
   * @param pos in the uri to place the value.
   * @param value to insert.
   * @return a RequestTemplate for chaining.
   * @deprecated see {@link RequestTemplate#target(String)}
   */
  @SuppressWarnings("unused")
  @Deprecated
  public RequestTemplate insert(int pos, CharSequence value) {
    return target(value.toString());
  }

  /**
   * Set the Uri for the request, replacing the existing uri if set.
   *
   * @param uri to use, must be a relative uri.
   * @return a RequestTemplate for chaining.
   */
  public RequestTemplate uri(String uri) {
    return this.uri(uri, false);
  }

  /**
   * Set the uri for the request.
   *
   * @param uri to use, must be a relative uri.
   * @param append if the uri should be appended, if the uri is already set.
   * @return a RequestTemplate for chaining.
   */
  // 4
  public RequestTemplate uri(String uri, boolean append) {
    /* validate and ensure that the url is always a relative one */
    uri = UriInitial.validate(uri); // 1

    /*
     * templates may provide query parameters. since we want to manage those explicity, we will need
     * to extract those out, leaving the uriTemplate with only the path to deal with.
     */
    Matcher queryMatcher = QUERY_STRING_PATTERN.matcher(uri);
    if (queryMatcher.find()) {
      String queryString = uri.substring(queryMatcher.start() + 1);

      /* parse the query string */
      this.extractQueryTemplates(queryString, append);

      /* reduce the uri to the path */
      uri = uri.substring(0, queryMatcher.start());
    }

    int fragmentIndex = uri.indexOf('#');
    if (fragmentIndex > -1) {  // 1
      fragment = uri.substring(fragmentIndex);
      uri = uri.substring(0, fragmentIndex);
    }

    /* replace the uri template */
    if (append && this.uriTemplate != null) {  // 1
      this.uriTemplate = UriTemplate.append(this.uriTemplate, uri);
    } else {  // 1
      this.uriTemplate = UriTemplate.create(uri, !this.decodeSlash, this.charset);
    }
    return this;
  }

  /**
   * Set the target host for this request.
   *
   * @param target host for this request. Must be an absolute target.
   * @return a RequestTemplate for chaining.
   */
  // 4
  public RequestTemplate target(String target) {

    Optional<URI> optionalURI = TargetUtil.generateURI(target);// 1
    if(!optionalURI.isPresent()) return this; // 1
    final URI targetUri = optionalURI.get();

    if (Util.isNotBlank(targetUri.getRawQuery())) { // 1
      /*
       * target has a query string, we need to make sure that they are recorded as queries
       */
      this.extractQueryTemplates(targetUri.getRawQuery(), true);
    }
    /* strip the query string */
    this.target = targetUri.getScheme() + "://" + targetUri.getAuthority() + targetUri.getPath();
    if (targetUri.getFragment() != null) { // 1
      this.fragment = "#" + targetUri.getFragment();
    }

    return this;
  }

  /**
   * The URL for the request. If the template has not been resolved, the url will represent a uri
   * template.
   *
   * @return the url
   */
  // 2
  public String url() {

    /* build the fully qualified url with all query parameters */
    StringBuilder url = new StringBuilder(this.path());
    if (!this.queryRequest.getQueries().isEmpty()) { // 1
      url.append(this.queryLine());
    }
    if (fragment != null) { // 1
      url.append(fragment);
    }

    return url.toString();
  }

  /**
   * The Uri Path.
   *
   * @return the uri path.
   */
  // 3
  public String path() {
    /* build the fully qualified url with all query parameters */
    StringBuilder path = new StringBuilder();
    if (this.target != null) {  // 1
      path.append(this.target);
    }
    if (this.uriTemplate != null) {  // 1
      path.append(this.uriTemplate.toString());
    }
    if (path.length() == 0) {  // 1
      /* no path indicates the root uri */
      path.append("/");
    }
    return path.toString();

  }

  /**
   * List all of the template variable expressions for this template.
   *
   * @return a list of template variable names
   */
  // 3
  public List<String> variables() {
    /* combine the variables from the uri, query, header, and body templates */
    List<String> variables = new ArrayList<>(this.uriTemplate.getVariables());

    /* queries */
    for (QueryTemplate queryTemplate : this.queryRequest.getQueries().values()) {  // 1
      variables.addAll(queryTemplate.getVariables());
    }

    /* headers */
    for (HeaderTemplate headerTemplate : this.headerRequest.getHeaders().values()) {  // 1
      variables.addAll(headerTemplate.getVariables());
    }

    /* body */
    if (this.bodyTemplate != null) {  // 1
      variables.addAll(this.bodyTemplate.getVariables());
    }

    return variables;
  }

  /**
   * @see RequestTemplate#query(String, Iterable)
   */
  // 1
  public RequestTemplate query(String name, String... values) {
    if (values == null) {  // 1
      return query(name, Collections.emptyList());
    }
    return query(name, Arrays.asList(values));
  }


  /**
   * Specify a Query String parameter, with the specified values. Values can be literals or template
   * expressions.
   *
   * @param name of the parameter.
   * @param values for this parameter.
   * @return a RequestTemplate for chaining.
   */
  public RequestTemplate query(String name, Iterable<String> values) {
    return appendQuery(name, values, this.collectionFormat);
  }

  /**
   * Specify a Query String parameter, with the specified values. Values can be literals or template
   * expressions.
   *
   * @param name of the parameter.
   * @param values for this parameter.
   * @param collectionFormat to use when resolving collection based expressions.
   * @return a Request Template for chaining.
   */
  public RequestTemplate query(String name,
                               Iterable<String> values,
                               CollectionFormat collectionFormat) {
    return appendQuery(name, values, collectionFormat);
  }

  /**
   * Appends the query name and values.
   *
   * @param name of the parameter.
   * @param values for the parameter, may be expressions.
   * @param collectionFormat to use when resolving collection based query variables.
   * @return a RequestTemplate for chaining.
   */
  // 4
  private RequestTemplate appendQuery(String name,
                                      Iterable<String> values,
                                      CollectionFormat collectionFormat) {
    if (!values.iterator().hasNext()) {  // 1
      /* empty value, clear the existing values */
      this.queryRequest.getQueries().remove(name);
      return this;
    }

    /* create a new query template out of the information here */
    this.queryRequest.getQueries().compute(name, (key, queryTemplate) -> {  // 1
      if (queryTemplate == null) {  // 1
        return QueryTemplate.create(name, values, this.charset, collectionFormat, this.decodeSlash);
      } else {  // 1
        return QueryTemplate.append(queryTemplate, values, collectionFormat, this.decodeSlash);
      }
    });
    return this;
  }

  /**
   * Sets the Query Parameters.
   *
   * @param queries to use for this request.
   * @return a RequestTemplate for chaining.
   */
  @SuppressWarnings("unused")
  public RequestTemplate queries(Map<String, Collection<String>> queries) {
    return queryRequest.queries(this, queries);
  }


  /**
   * Return an immutable Map of all Query Parameters and their values.
   *
   * @return registered Query Parameters.
   */
  // 1
  public Map<String, Collection<String>> queries() {
    return queryRequest.queries();
  }

  /**
   * @see RequestTemplate#header(String, Iterable)
   */
  public RequestTemplate header(String name, String... values) {
    return header(name, Arrays.asList(values));
  }

  /**
   * Add a header using the supplied Chunks.
   *
   * @param name of the header.
   * @param chunks to add.
   * @return a RequestTemplate for chaining.
   */
  public RequestTemplate header(String name, TemplateChunk... chunks) {
    headerRequest.header(name, chunks);
    return this;
  }

  /**
   * Specify a Header, with the specified values. Values can be literals or template expressions.
   *
   * @param name of the header.
   * @param values for this header.
   * @return a RequestTemplate for chaining.
   */
  // 2
  public RequestTemplate header(String name, Iterable<String> values) {
    headerRequest.header(name, values);
    return this;
  }

  /**
   * Clear on reader from {@link RequestTemplate}
   *
   * @param name of the header.
   * @return a RequestTemplate for chaining.
   */
  // 1
  public RequestTemplate removeHeader(String name) {
    this.headerRequest.removeHeader(name);
    return this;
  }

  /**
   * Headers for this Request.
   *
   * @param headers to use.
   * @return a RequestTemplate for chaining.
   */
  // 2
  public RequestTemplate headers(Map<String, Collection<String>> headers) {
    this.headerRequest.headers(headers);
    return this;
  }

  /**
   * Returns an immutable copy of the Headers for this request.
   *
   * @return the currently applied headers.
   */
  // 2
  public Map<String, Collection<String>> headers() {
    Map<String, Collection<String>> headerMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    this.headerRequest.getHeaders().forEach((key, headerTemplate) -> { // 1
      List<String> values = new ArrayList<>(headerTemplate.getValues());

      /* add the expanded collection, but only if it has values */
      if (!values.isEmpty()) { // 1
        headerMap.put(key, Collections.unmodifiableList(values));
      }
    });
    return Collections.unmodifiableMap(headerMap);
  }

  /**
   * Sets the Body and Charset for this request.
   *
   * @param data to send, can be null.
   * @param charset of the encoded data.
   * @return a RequestTemplate for chaining.
   */
  public RequestTemplate body(byte[] data, Charset charset) {
    this.body(Request.Body.create(data, charset));
    return this;
  }

  /**
   * Set the Body for this request. Charset is assumed to be UTF_8. Data must be encoded.
   *
   * @param bodyText to send.
   * @return a RequestTemplate for chaining.
   */
  public RequestTemplate body(String bodyText) {
    this.body(Request.Body.create(bodyText.getBytes(this.charset), this.charset));
    return this;
  }

  /**
   * Set the Body for this request.
   *
   * @param body to send.
   * @return a RequestTemplate for chaining.
   * @deprecated use {@link #body(byte[], Charset)} instead.
   */
  @Deprecated
  // 1
  public RequestTemplate body(Request.Body body) {
    this.body = body;

    /* body template must be cleared to prevent double processing */
    this.bodyTemplate = null;

    header(CONTENT_LENGTH, Collections.emptyList());
    if (body.length() > 0) { // 1
      header(CONTENT_LENGTH, String.valueOf(body.length()));
    }

    return this;
  }

  /**
   * Charset of the Request Body, if known.
   *
   * @return the currently applied Charset.
   */
  // 1
  public Charset requestCharset() {
    if (this.body != null) { // 1
      return this.body.getEncoding()
          .orElse(this.charset);
    }
    return this.charset;
  }

  /**
   * The Request Body.
   *
   * @return the request body.
   */
  public byte[] body() {
    return body.asBytes();
  }

  /**
   * The Request.Body internal object.
   *
   * @return the internal Request.Body.
   * @deprecated this abstraction is leaky and will be removed in later releases.
   */
  @Deprecated
  public Request.Body requestBody() {
    return this.body;
  }


  /**
   * Specify the Body Template to use. Can contain literals and expressions.
   *
   * @param bodyTemplate to use.
   * @return a RequestTemplate for chaining.
   */
  public RequestTemplate bodyTemplate(String bodyTemplate) {
    this.bodyTemplate = BodyTemplate.create(bodyTemplate, this.charset);
    return this;
  }

  /**
   * Specify the Body Template to use. Can contain literals and expressions.
   *
   * @param bodyTemplate to use.
   * @return a RequestTemplate for chaining.
   */
  public RequestTemplate bodyTemplate(String bodyTemplate, Charset charset) {
    this.bodyTemplate = BodyTemplate.create(bodyTemplate, charset);
    this.charset = charset;
    return this;
  }

  /**
   * Body Template to resolve.
   *
   * @return the unresolved body template.
   */
  // 1
  public String bodyTemplate() {
    if (this.bodyTemplate != null) { //1
      return this.bodyTemplate.toString();
    }
    return null;
  }

  @Override
  public String toString() {
    return request().toString();
  }

  /**
   * Return if the variable exists on the uri, query, or headers, in this template.
   *
   * @param variable to look for.
   * @return true if the variable exists, false otherwise.
   */
  public boolean hasRequestVariable(String variable) {
    return this.getRequestVariables().contains(variable);
  }

  /**
   * Retrieve all uri, header, and query template variables.
   *
   * @return a List of all the variable names.
   */
  // 2
  public Collection<String> getRequestVariables() {
    final Collection<String> variables = new LinkedHashSet<>(this.uriTemplate.getVariables());
    this.queryRequest.getQueries().values().forEach(queryTemplate -> variables.addAll(queryTemplate.getVariables())); // 1
    this.headerRequest.getHeaders().values()
        .forEach(headerTemplate -> variables.addAll(headerTemplate.getVariables())); // 1
    return variables;
  }

  /**
   * If this template has been resolved.
   *
   * @return true if the template has been resolved, false otherwise.
   */
  @SuppressWarnings("unused")
  public boolean resolved() {
    return this.resolved;
  }

  /**
   * The Query String for the template. Expressions are not resolved.
   *
   * @return the Query String.
   */
  public String queryLine() {
    return queryRequest.queryLine();
  }
  //3
  private void extractQueryTemplates(String queryString, boolean append) {
    /* split the query string up into name value pairs */
    Map<String, List<String>> queryParameters = ExtractQueryTemplate.extract(queryString); // 1

    /* add them to this template */
    if (!append) { // 1
      /* clear the queries and use the new ones */
      this.queryRequest.getQueries().clear();
    }
    queryParameters.forEach(this::query); // 1
  }

  @Experimental
  public RequestTemplate methodMetadata(MethodMetadata methodMetadata) {
    this.methodMetadata = methodMetadata;
    return this;
  }

  @Experimental
  public RequestTemplate feignTarget(Target<?> feignTarget) {
    this.feignTarget = feignTarget;
    return this;
  }

  @Experimental
  public MethodMetadata methodMetadata() {
    return methodMetadata;
  }

  @Experimental
  public Target<?> feignTarget() {
    return feignTarget;
  }

  /**
   * Factory for creating RequestTemplate.
   */
  interface Factory {

    /**
     * create a request template using args passed to a method invocation.
     */
    RequestTemplate create(Object[] argv);
  }

}