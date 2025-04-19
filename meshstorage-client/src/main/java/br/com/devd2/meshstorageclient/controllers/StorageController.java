package br.com.devd2.meshstorageclient.controllers;

import br.com.devd2.meshstorage.models.StorageClient;
import br.com.devd2.meshstorageclient.config.StorageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StorageController {

    @Autowired
    private StorageConfig storageConfig;

    @GetMapping("/")
    public String statusStorage(Model model) {
        StorageClient storageClient = storageConfig.getClient();
        model.addAttribute("storage", storageClient);
        return "index";
    }

}