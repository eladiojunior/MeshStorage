package br.com.devd2.meshstorageserver.models;

import lombok.Data;

@Data
public class MetricsStorageModel {
    private Long totalSpace;  // em MB
    private Long freeSpace;   // em MB (Megabytes)
    private Long responseTime;  // em Ms (Miléssimo de segundo)
    private Integer requestLastMinute;
    private Integer errosLastRequest;
    private boolean available;
}
