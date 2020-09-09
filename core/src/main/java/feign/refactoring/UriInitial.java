package feign.refactoring;

import feign.template.UriUtils;

import java.util.Arrays;

//7
public enum UriInitial {
    BARRA("/"),
    CHAVE("{"),
    INTERROGACAO("?"),
    PONTO_VIRGULA(";");

    private final String value;

    UriInitial(String value) {
        this.value = value;
    }

    String getValue() {
        return value;
    }

    public static String validate(String value){
        if (UriUtils.isAbsolute(value)) { // 2
            throw new IllegalArgumentException("url values must be not be absolute.");
        }
        if(value == null) return  BARRA.getValue(); // 1
        if(value.isEmpty()
                && Arrays.stream(UriInitial.values())
                .map(UriInitial::getValue)
                .noneMatch(value::startsWith)){ //4
            return BARRA.getValue() + value;
        }
        return value;
    }
}
