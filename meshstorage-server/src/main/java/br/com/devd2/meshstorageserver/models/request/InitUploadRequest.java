package br.com.devd2.meshstorageserver.models.request;

public record InitUploadRequest(
        String applicationCode,
        String fileName,
        String contentType,
        long fileSize,
        String checksumSha256 // opcional
) {}
