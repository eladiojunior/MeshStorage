package br.com.devd2.meshstorageserver.helper;

import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;

public class HelperFileType {

    /**
     * Valida um ContentType informado.
     * @param contentType - Content Type para validar.
     * @return true, valido ou false, inválido.
     */
    public static boolean isValidContentType(String contentType) {
        try {
            if (contentType == null || contentType.isEmpty())
                return false;
            MediaType.parseMediaType(contentType);
            return true;
        } catch (InvalidMediaTypeException erro) {
            return false;
        }
    }

}
