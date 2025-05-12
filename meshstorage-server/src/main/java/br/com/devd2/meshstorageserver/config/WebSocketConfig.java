package br.com.devd2.meshstorageserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${limit-mb}")
    private int limitFilesMB;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/server-storage-websocket");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/server");
        registry.enableSimpleBroker("/client");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        int limit = limitFilesMB * 1024 * 1024;  // MB em bytes
        registry.setMessageSizeLimit(limit);     // tamanho do frame
        registry.setSendBufferSizeLimit(limit);  // buffer por sessão
        registry.setSendTimeLimit(30_000);       // 30s para empurrar t_odo o payload
    }

}