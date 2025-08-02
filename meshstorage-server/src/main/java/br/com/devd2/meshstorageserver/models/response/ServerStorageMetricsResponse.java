package br.com.devd2.meshstorageserver.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServerStorageMetricsResponse {
    private Long totalSpace;  // em MB
    private Long freeSpace;   // em MB (Megabytes)
    private Long responseTime;  // em Ms (Miléssimo de segundo)

    private Integer requestLastMinute;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dateTimeLastRequest;

    private Integer errosLastRequest;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dateTimeLastAvailable;
}