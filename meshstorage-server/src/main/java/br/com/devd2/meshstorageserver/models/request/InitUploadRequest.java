package br.com.devd2.meshstorageserver.models.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record InitUploadRequest(
        @Schema(description = "Cógigo/Sigla da aplicação que está enviando o upload", example = "APP01")
        String applicationCode,
        @Schema(description = "Nome do arquivo de upload", example = "NomeArquivo.ext")
        String fileName,
        @Schema(description = "Tipo do arquivo (ContentType)", example = "application/pdf")
        String contentType,
        @Schema(description = "Tamanho em bytes do do arquivo de upload", example = "100000 = 100KB")
        long fileSize,
        @Schema(description = "Hash do arquivo para verificação", example = "HASH Sha256")
        String checksumSha256 // opcional
) {}
