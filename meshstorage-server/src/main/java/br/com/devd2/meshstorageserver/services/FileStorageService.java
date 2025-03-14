package br.com.devd2.meshstorageserver.services;

import br.com.devd2.meshstorageserver.entites.FileStorage;
import br.com.devd2.meshstorageserver.exceptions.ApiBusinessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    public FileStorage getFile(String idFile)throws ApiBusinessException {

        if (idFile == null || idFile.isEmpty())
            throw new ApiBusinessException("Id File (chave do arquivo) não pode ser nulo ou vazio.");

        return null;

    }

    public FileStorage registerFile(String applicationName, MultipartFile file) throws ApiBusinessException {

        if (applicationName == null || applicationName.isEmpty())
            throw new ApiBusinessException("Application Name (nome da aplicação) não pode ser nulo ou vazio.");

        if (file == null || file.isEmpty())
            throw new ApiBusinessException("File (arquivo físico) não pode ser nulo ou vazio.");

        return null;

    }

}