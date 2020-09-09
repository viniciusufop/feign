package feign.refactoring;

import feign.template.HeaderTemplate;

import java.util.Optional;
// 3 TODO pouca complexidade
public final class HeaderTemplateValue {
    private final HeaderTemplate headerTemplate; //1
    private final String value;

    public HeaderTemplateValue(HeaderTemplate headerTemplate, String value) {
        this.headerTemplate = headerTemplate;
        this.value = value;
    }

    public Optional<HeaderTemplateValue> splitHeaderValue() {
        if(value.isEmpty()) return Optional.empty(); // 1
        final String newValue = value.substring(value.indexOf(" ") + 1);
        if(newValue.isEmpty()) return Optional.empty(); // 1
        return Optional.of(new HeaderTemplateValue(headerTemplate, newValue));
    }

    public HeaderTemplate getHeaderTemplate() {
        return headerTemplate;
    }

    public String getValue() {
        return value;
    }
}
