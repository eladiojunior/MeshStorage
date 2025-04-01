package br.com.devd2.meshstorageclient;

import br.com.devd2.meshstorageclient.config.StorageConfig;
import br.com.devd2.meshstorageclient.helper.UtilClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.ConnectException;
import java.util.Scanner;

@EnableScheduling //Ativa a execução de tarefas agendadas
@SpringBootApplication
public class MeshstorageClientApplication {

    public static void main(String[] args) {

        System.out.println("+--------------------------------------------------------------------+");
        System.out.println("| Configuração do Client do MeshStorage --------------------- v1.0.0 |");
        System.out.println("+--------------------------------------------------------------------+");
        try {
            verificarParametros(args);
            System.out.println("+--------------------------------------------------------------------+");
            System.out.println("| Configuração realizada com sucesso!                                |");
            System.out.println("+--------------------------------------------------------------------+");
            SpringApplication.run(MeshstorageClientApplication.class, args);
        } catch (Exception erro) {
            System.out.println("Erro: " + erro.getMessage());
            System.exit(1);
        }
    }

    private static void verificarParametros(String[] args) throws ConnectException {

        var server = getParametro("-server", args);
        var storage = getParametro("-storage", args);

        try (var scanner = new Scanner(System.in)) {
            if (server.isBlank()) {
                var nomeServidorDefault = UtilClient.getMachineName();
                System.out.printf(">> Nome Servidor [%s]: ", nomeServidorDefault);
                server = scanner.nextLine().trim();
                if (server.isBlank())
                    server = nomeServidorDefault;
            } else {
                System.out.println(String.format(">> Nome Servidor: %s", server));
            }
            if (storage.isBlank()) {
                System.out.print(">> Local de Armazenamento: ");
                storage = scanner.nextLine().trim();
                if (storage.isBlank()) {
                   throw new IllegalArgumentException("Local de armazenamento não informado.");
                }
            } else {
                System.out.println(String.format(">> Local de Armazenamento: %s", storage));
            }
        }
        if (!UtilClient.isStorageValid(storage)) {
            throw new IllegalArgumentException("Local de armazenamento inválido ou inexistente.");
        }

        StorageConfig storageConfig;
        try (AnnotationConfigApplicationContext context =
            new AnnotationConfigApplicationContext("br.com.devd2.meshstorageclient.config")) {
            storageConfig = context.getBean(StorageConfig.class);
        }
        storageConfig.getClient().setStorageName(storage);

        storageConfig.getClient().setServerName(server);
        var ipMaquina = UtilClient.getMachineIp();
        System.out.println(String.format(">> IP Servidor: %s", ipMaquina));
        storageConfig.getClient().setIpServer(ipMaquina);

        var nomeOs = UtilClient.getOperatingSystem();
        System.out.println(String.format(">> Sistema Operacional: %s", nomeOs));
        storageConfig.getClient().setOsName(nomeOs);

        if (!storageConfig.testConnectServer()) {
            throw new ConnectException("Conexão com o servidor MeshStorage não realizada, verifique as configurações.");
        }
    }

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
