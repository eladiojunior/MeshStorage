package br.com.devd2.meshstorageserver.models.response;

import br.com.devd2.meshstorage.enums.ExtractionTextByOcrStatusEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FileOcrExtractionResponse {

    /**
     * Conteúdo texto da extração do OCR.
     */
    private String contentTextByOcr;

    /**
     * Hash do conteúdo texto da extração do OCR.
     */
    private String hashContentTextByOcr;

    /**
     * Codigo do Status do processo de extração de texto do arquivos, via OCR.
     * {@link ExtractionTextByOcrStatusEnum}
     */
    private Integer codeStatusExtractionTextByOrc = ExtractionTextByOcrStatusEnum.IN_PROCESSING.getCode();

    /**
     * Descrição do Status do processo de extração de texto do arquivos, via OCR.
     * {@link ExtractionTextByOcrStatusEnum}
     */
    private String descriptionStatusExtractionTextByOrc = ExtractionTextByOcrStatusEnum.IN_PROCESSING.getDescription();

    /**
     * Nome tipo de documento do conteúdo extraido do OCR.
     * Exemplo: Nota fiscal, CPF, Identidade (RG), Passaporte etc.
     * Caso o Ocr não reconheça um tipo será retornado "Não identificado"
     */
    private String nameTypeDocumentByOcr;

    /**
     * Percentual (0 - 1) do grau de confiança do tipo de documento extraído do conteúdo do OCR.
     * Quanto maior mais confiável o tipo identificado.
     */
    private double percentualConfidenceTypeDocumentByOcr;

    /**
     * Data e hora do início do processo de OCR.
     */
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dateTimeStartExtractionByOcr;

    /**
     * Data e hora do final do processo de OCR.
     */
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dateTimeEndExtractionByOcr;

    /**
     * Lista de campos (chave e valor) extraído do processo de OCR.
     */
    private List<FileOcrExtractionFieldsResponse> fieldsExtractionByOcr;
}