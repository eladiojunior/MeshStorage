package br.com.devd2.meshstorageserver.helper;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

public class HelperFormat {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");
    private static final DecimalFormat ONE_DECIMAL = (DecimalFormat)NumberFormat.getNumberInstance(LOCALE_BR);

    static {
        ONE_DECIMAL.applyPattern("#0.0");
    }

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
    public static String formatDateTime(LocalDateTime dateTime, String pattern, Locale locale) {
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

    /**
     *  Sobrecarga prática usando o locale default.
     * */
    public static String formatDateTime(LocalDateTime dateTime, String pattern) {
        return formatDateTime(dateTime, pattern, null);
    }

    /**
     * Formata um valor decimal em formato de perdentual: 0.0;
     *
     * @return “80,5%” ou “19,5%”
     **/
    public static String formatPercent(double percentual) {
        if (percentual <= 0)
            return "0%";
        return ONE_DECIMAL.format(percentual) + '%';
    }

}