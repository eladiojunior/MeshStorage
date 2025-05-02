package br.com.devd2.meshstorageclient;

import br.com.devd2.meshstorageclient.components.TimerTaskStatusClient;
import br.com.devd2.meshstorageclient.config.StorageConfig;
import br.com.devd2.meshstorageclient.helper.UtilClient;

import java.net.ConnectException;
import java.util.Scanner;
import java.util.Timer;

public class MeshstorageClientMain {

    public static void main(String[] args) {

        System.out.println("+--------------------------------------------------------------------+");
        System.out.println("| Configuração do Client do MeshStorage --------------------- v1.0.0 |");
        System.out.println("+--------------------------------------------------------------------+");
        try {

            StorageConfig storageConfig = StorageConfig.get();
            if (storageConfig != null && storageConfig.isExistendClient()) {
                System.out.printf(">> Url Websocket Server: %s%n", storageConfig.getClient().getUrlWebsocketServer());
                System.out.printf(">> Nome Servidor: %s%n", storageConfig.getClient().getServerName());
                System.out.printf(">> IP Servidor: %s%n", storageConfig.getClient().getIpServer());
                System.out.printf(">> Sistema Operacional: %s%n", storageConfig.getClient().getOsName());
                System.out.printf(">> Nome do Armazenamento: %s%n", storageConfig.getClient().getStorageName());
                System.out.printf(">> Local de Armazenamento: %s%n", storageConfig.getClient().getStoragePath());
            } else {
                verificarParametros(args, storageConfig);
            }

            if (storageConfig != null && storageConfig.notConnectServer()) {
                throw new ConnectException("Conexão com o servidor MeshStorage não realizada, verifique as configurações.");
            }

            System.out.println("+--------------------------------------------------------------------+");
            System.out.println("| Configuração realizada, MeshStorageClient iniciado com sucesso!    |");
            System.out.println("+--------------------------------------------------------------------+");

            //Inicializar time para enviar Status do Client para o Server.
            new Timer().scheduleAtFixedRate(new TimerTaskStatusClient(), 5000, 5000);

            // Manter a aplicação rodando
            Thread.currentThread().join();

        } catch (Exception error) {
            System.err.println("Erro: " + error.getMessage());
            System.out.println("+--------------------------------------------------------------------+");
            System.out.println("| MeshStorageClient NÃO inicializado!                                |");
            System.out.println("+--------------------------------------------------------------------+");
            System.exit(1);
        }
    }

    /**
     * Verifica os paramentros enviados no Main pela usuário.
     * @param args Array de argumentos do Main
     * @param storageConfig Instancia do StorageConfig.
     * @throws ConnectException Caso identificado erro na conexão com o WebSocket do Servidor.
     */
    private static void verificarParametros(String[] args, StorageConfig storageConfig) throws ConnectException {

        String urlWebsocketServer = getParametro("-url-websocket-server", args);
        String server = getParametro("-server-name", args);
        String storageName = getParametro("-storage-name", args);
        String storagePath = getParametro("-storage-path", args);

        try (Scanner scanner = new Scanner(System.in)) {

            if (urlWebsocketServer.isEmpty()) {
                String urlWebsocketServerDefault = UtilClient.getUrlWebsocketServer();
                System.out.printf(">> Url Websocket Server [%s]: ", urlWebsocketServerDefault);
                urlWebsocketServer = scanner.nextLine().trim();
                if (urlWebsocketServer.isEmpty())
                    urlWebsocketServer = urlWebsocketServerDefault;
            } else {
                System.out.printf(">> Url Websocket Server: %s%n", urlWebsocketServer);
            }

            if (server.isEmpty()) {
                String nomeServidorDefault = UtilClient.getMachineName();
                System.out.printf(">> Nome Servidor [%s]: ", nomeServidorDefault);
                server = scanner.nextLine().trim();
                if (server.isEmpty())
                    server = nomeServidorDefault;
            } else {
                System.out.printf(">> Nome Servidor: %s%n", server);
            }

            if (storageName.isEmpty()) {
                String nomeStorageDefault = "STORAGE1";
                System.out.printf(">> Nome do Armazenamento [%s]: ", nomeStorageDefault);
                storageName = scanner.nextLine().trim();
                if (storageName.isEmpty())
                    storageName = nomeStorageDefault;
                if (storageName.isEmpty()) {
                    throw new IllegalArgumentException("Nome do armazenamento não informado.");
                }
            } else {
                System.out.printf(">> Nome do Armazenamento: %s%n", storageName);
            }

            if (storagePath.isEmpty()) {
                System.out.print(">> Local de Armazenamento: ");
                storagePath = scanner.nextLine().trim();
                if (storagePath.isEmpty()) {
                   throw new IllegalArgumentException("Local de armazenamento não informado.");
                }
            } else {
                System.out.printf(">> Local de Armazenamento: %s%n", storagePath);
            }
        }
        if (!UtilClient.isStorageValid(storagePath)) {
            throw new IllegalArgumentException("Local de armazenamento inválido ou inexistente.");
        }

        String ipMaquina = UtilClient.getMachineIp();
        System.out.printf(">> IP Servidor: %s%n", ipMaquina);
        String nomeOs = UtilClient.getOperatingSystem();
        System.out.printf(">> Sistema Operacional: %s%n", nomeOs);

        storageConfig.getClient().setIdClient(UtilClient.generateHashIdClient(server, storageName));
        storageConfig.getClient().setUrlWebsocketServer(urlWebsocketServer);
        storageConfig.getClient().setServerName(server);
        storageConfig.getClient().setStorageName(storageName);
        storageConfig.getClient().setStoragePath(storagePath);
        storageConfig.getClient().setIpServer(ipMaquina);
        storageConfig.getClient().setOsName(nomeOs);

        storageConfig.gravarStorageServer();

    }

    /**
     * Recupera o parametro informado do array de Args do Main.
     * @param nameParam Nome do parametro esperado
     * @param args - Array de argumentos do Main
     * @return Valor do parametro ou vazio se não existir
     */
    private static String getParametro(String nameParam, String[] args) {
        String resultValue = "";
        for (int i = 0; i < args.length; i++) {
            if (nameParam.equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                resultValue = args[i + 1];
            }
        }
        return resultValue;
    }

}
