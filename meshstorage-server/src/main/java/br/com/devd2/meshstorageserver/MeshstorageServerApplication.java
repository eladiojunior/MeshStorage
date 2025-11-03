package br.com.devd2.meshstorageserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MeshstorageServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeshstorageServerApplication.class, args);
    }

}
