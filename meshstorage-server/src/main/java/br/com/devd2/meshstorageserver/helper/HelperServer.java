package br.com.devd2.meshstorageserver.helper;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class HelperServer {
    private static final String IP_REGEX =
            "^((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])\\.){3}"
                    + "(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])$";

    /**
     * Verifica se um IP informado é valido, utilizando Regex
     * @param ip - Endereço de IP a ser verificado.
     * @return true=Válido, false=Inválido.
     */
    public static boolean IsValidIp(String ip) {
        return ip != null && ip.matches(IP_REGEX);
    }

    /**
     * Responsável por calulcar o tempo de resposta em milissegundos (long).
     * @param start - LocalDateTime inicial (t1).
     * @param end - LocalDateTime final (t2).
     * @return tempo de resposta (long);
     */
    public static long elapsedMillis(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.MILLIS.between(start, end);
    }

    /**
     * Responsável por calulcar o tempo de resposta em minutos (long).
     * @param start - LocalDateTime inicial (t1).
     * @param end - LocalDateTime final (t2).
     * @return tempo de resposta (long);
     */
    public static long elapsedMinutes(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.MINUTES.between(start, end);
    }
}
