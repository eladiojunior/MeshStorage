package br.com.devd2.meshstorageserver.models;

import lombok.Data;

import java.nio.file.Path;
import java.time.Instant;
import java.util.BitSet;
@Data
public class UploadSessionModel {
    private final String uploadId;
    private final String applicationCode;
    private final String fileName;
    private final String contentType;
    private final long size;
    private final int chunkSize;
    private final long totalChunks;
    private final Path stagingFile;      // arquivo destino em staging
    private final BitSet received;       // marca chunks recebidos
    private final Instant createdAt;
    private volatile Instant lastTouch;
    private final String expectedSha256; // opcional

    public UploadSessionModel(String uploadId, String applicationCode, String fileName, String contentType, long size,
                              int chunkSize, long totalChunks, Path stagingFile, String expectedSha256) {
        this.uploadId = uploadId;
        this.applicationCode = applicationCode;
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
        this.chunkSize = chunkSize;
        this.totalChunks = totalChunks;
        this.stagingFile = stagingFile;
        this.received = new BitSet((int) totalChunks);
        this.createdAt = Instant.now();
        this.lastTouch = this.createdAt;
        this.expectedSha256 = expectedSha256;
    }
    public boolean isComplete() {
        return received.cardinality() == totalChunks;
    }
}
