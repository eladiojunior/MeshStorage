package br.com.devd2.meshstorageclient;

import br.com.devd2.meshstorageclient.config.StorageConfig;
import br.com.devd2.meshstorageclient.models.StorageClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling //Ativa a execução de tarefas agendadas
@SpringBootApplication
public class MeshstorageClientApplication {

    public static void main(String[] args) {
        System.out.println("+--------------------------------------------------------------------+");
        System.out.println("| Configuração do Client do MeshStorage --------------------- v1.0.0 |");
        System.out.println("+--------------------------------------------------------------------+");
        verificarParametros(args);
        SpringApplication.run(MeshstorageClientApplication.class, args);
    }

    private static void verificarParametros(String[] args) {
        if (args==null || args.length == 0) {
            System.out.println("==> Não identificamos os parâmetros, por favor informe.");
            System.out.println("----------------------------------------------------------------------");
            var nomeServidorDefault = "";//UtilClient.obterNomeServidor();
            System.out.printf("Nome Servidor [%n]", nomeServidorDefault);
            var nomeServidor = System.console().readLine();
            StorageConfig.get().getClient().setIpServer("127.0.0.1");
        }
    }

}
