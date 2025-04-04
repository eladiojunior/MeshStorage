package br.com.devd2.meshstorageserver.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/status-update-client")
    public void receiveStatusUpdate(String message) {

        System.out.println("Status recebido: " + message);

    }

    // Comando para um file server armazenar um arquivo
    @MessageMapping("/status-upload-file")
    public void receiverStatusUpdateFile(String fileInfo) {

        System.out.println("Status file: " + fileInfo);

    }
}
