package br.com.devd2.meshstorageserver.helper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HelperSessionClients {
    private static HelperSessionClients instance;
    private final Map<String, String> sessionToClientMap;
    private HelperSessionClients() {
        sessionToClientMap = new ConcurrentHashMap<>();
    }
    public static HelperSessionClients get() {
        if (instance == null) {
            instance = new HelperSessionClients();
        }
        return instance;
    }
    public boolean hasSessionToIdClient(String idClient) {
        return sessionToClientMap.containsValue(idClient);
    }
    public void addSessionToClient(String sessionId, String clientId) {
        sessionToClientMap.put(sessionId, clientId);
    }
    public String getIdClient(String sessionId) {
        return sessionToClientMap.get(sessionId);
    }
    public void removeSessionToClient(String sessionId) {
        sessionToClientMap.remove(sessionId);
    }
}