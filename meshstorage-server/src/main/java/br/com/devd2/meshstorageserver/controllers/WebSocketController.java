package br.com.devd2.meshstorageserver.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    // Recebe atualizações de espaço dos file servers
    @MessageMapping("/status-update")
    @SendTo("/topic/storage-status")
    public String receiveStatusUpdate(String message) {
        System.out.println("Status recebido: " + message);
        return "Atualização recebida: " + message;
    }

    // Comando para um file server armazenar um arquivo
    @MessageMapping("/store-file")
    @SendTo("/topic/store-command")
    public String sendStorageCommand(String fileInfo) {
        return "Armazene o arquivo: " + fileInfo;
    }
}
