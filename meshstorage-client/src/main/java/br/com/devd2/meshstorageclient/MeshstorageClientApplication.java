package br.com.devd2.meshstorageclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling //Ativa a execução de tarefas agendadas
@SpringBootApplication
public class MeshstorageClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeshstorageClientApplication.class, args);
    }

}
