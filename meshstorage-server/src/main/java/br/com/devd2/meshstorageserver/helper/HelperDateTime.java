package br.com.devd2.meshstorageserver.helper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

public class HelperDateTime {

    /**
     * Formata um {@link LocalDateTime} usando o padrão informado.
     *
     * @param dateTime data/hora a formatar (não pode ser {@code null})
     * @param pattern  padrão no estilo {@link java.time.format.DateTimeFormatter#ofPattern(String)},
     *                 ex.: "dd/MM/yyyy HH:mm:ss"
     * @param locale   locale opcional (p.ex. {@code Locale.forLanguageTag("pt-BR")});
     *                 quando {@code null} usa o locale default da JVM
     * @return string formatada
     *
     * @throws NullPointerException     se {@code dateTime} ou {@code pattern} forem nulos
     * @throws IllegalArgumentException se o padrão for inválido
     */
    public static String format(LocalDateTime dateTime, String pattern, Locale locale) {
        Objects.requireNonNull(dateTime, "dateTime");
        Objects.requireNonNull(pattern,  "pattern");

        try {
            DateTimeFormatter fmt = locale == null
                    ? DateTimeFormatter.ofPattern(pattern)
                    : DateTimeFormatter.ofPattern(pattern, locale);
            return dateTime.format(fmt);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid date pattern: " + pattern, ex);
        }
    }

    /** Sobrecarga prática usando o locale default. */
    public static String format(LocalDateTime dateTime, String pattern) {
        return format(dateTime, pattern, null);
    }

}