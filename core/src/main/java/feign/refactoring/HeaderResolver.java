package feign.refactoring;

import feign.RequestTemplate;
import feign.template.HeaderTemplate;
import feign.template.Literal;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
// 8
public final class HeaderResolver {

    public static void resolver(Map<String, HeaderTemplate> headers,
                                Map<String, ?> variables, RequestTemplate resolved) { //2
        if (headers == null || headers.isEmpty()) return;//1
        resolved.headers(Collections.emptyMap());
        headers.values()
                .stream()
                .map(headerTemplate -> new HeaderTemplateValue(headerTemplate, headerTemplate.expand(variables)))// 1
                .map(HeaderTemplateValue::splitHeaderValue) // 1
                .filter(Optional::isPresent) // 1
                .map(Optional::get)// 1
                .forEach(headerTemplateValue ->
                        resolved.header(headerTemplateValue.getHeaderTemplate().getName(),
                                Literal.create(headerTemplateValue.getValue())));// 1
        //TODO tive que mudar o metodo header para public pq criei um pacote apartado
    }
}
