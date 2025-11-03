package br.com.devd2.meshstorageserver.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mesh.upload")
public record MeshUploadProps(
    long limitMb,
    String locationUploads,
    int chunkSize,
    String stagingDir,
    int sessionTtlMinutes,
    int maxConcurrentChunks
) {}
