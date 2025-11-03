package br.com.devd2.meshstorageserver.models.response;

public record InitUploadResponse(
        String uploadId, int chunkSize, long totalChunks)
{}
