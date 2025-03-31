package br.com.devd2.meshstorageclient;

import br.com.devd2.meshstorageclient.config.StorageConfig;
import br.com.devd2.meshstorageclient.helper.UtilClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

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

    private static void verificarParametros(String[] args) {

        var server = getParametro("-server", args);
        var storage = getParametro("-storage", args);

        try (var scanner = new Scanner(System.in)) {
            if (server.isBlank()) {
                var nomeServidorDefault = UtilClient.getMachineName();
                System.out.printf(">> Nome Servidor [%s]: ", nomeServidorDefault);
                server = scanner.nextLine().trim();
                if (server.isBlank())
                    server = nomeServidorDefault;
            }
            if (storage.isBlank()) {
                System.out.print(">> Local de Armazenamento: ");
                storage = scanner.nextLine().trim();
                if (storage.isBlank()) {
                   throw new IllegalArgumentException("Local de armazenamento não informado.");
                }
            }
        }

        StorageConfig.get().getClient().setServerName(server);
        StorageConfig.get().getClient().setIpServer(UtilClient.getMachineIp());
        StorageConfig.get().getClient().setOsServer(UtilClient.getOperatingSystem());
        StorageConfig.get().getClient().setStorageName(storage);

        if (!UtilClient.isStorageValid(StorageConfig.get().getClient().getStorageName())) {
            throw new IllegalArgumentException("Local de armazenamento inválido ou inexistente.");
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
