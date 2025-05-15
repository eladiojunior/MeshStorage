package br.com.devd2.meshstorage.models.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GenericMessage {
    @JsonProperty("type")
    private String type;
}