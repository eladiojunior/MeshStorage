package br.com.devd2.meshstorageserver.services;

import br.com.devd2.meshstorageserver.entites.Application;
import br.com.devd2.meshstorageserver.exceptions.ApiBusinessException;
import br.com.devd2.meshstorageserver.helper.HelperFileType;
import br.com.devd2.meshstorageserver.models.request.ApplicationRequest;
import br.com.devd2.meshstorageserver.repositories.ApplicationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    /**
     * Recupera uma Application (aplicação) pelo nome.
     * @param applicationName - Nome da aplicação;
     * @return Application ou nulo se não existir;
     */
    public Application findByName(String applicationName) {
        Optional<Application> application = applicationRepository.findByApplicationName(applicationName);
        return application.orElse(null);
    }

    /**
     * Registra uma aplicação (Application) com para armazenamento do ServerStorage;
     * @param request - Informações da aplicação.
     * @return Application criada;
     * @throws ApiBusinessException Erro de negócio
     */
    public Application registerApplication(ApplicationRequest request) throws ApiBusinessException {

        verificarRegrasNegocio(request);

        var application = applicationRepository.findByApplicationName(request.getApplicationName()).orElse(null);
        if (application != null)
            throw new ApiBusinessException(String.format("Existe uma aplicação [%1s] registrada com esse nome, não é permitido.", application.getApplicationName()));

        application = new Application();
        application.setApplicationName(request.getApplicationName());
        application.setApplicationDescription(request.getApplicationDescription());
        application.setAllowedFileTypes(String.join(";", request.getAllowedFileTypes()));
        application.setMaximumFileSizeMB(request.getMaximumFileSizeMB());
        application.setAllowDuplicateFile(request.isAllowDuplicateFile());
        application.setApplyOcrFileContent(request.isApplyOcrFileContent());
        application.setCompressFileContent(request.isCompressFileContent());
        application.setDateTimeApplication(LocalDateTime.now());

        return applicationRepository.save(application);

    }

    /**
     * Realizar atualização da aplicação com para armazenamento do ServerStorage;
     * @param idApplication - Identificador da aplicação.
     * @param request - Informações da aplicação.
     * @return Application atualizada;
     * @throws ApiBusinessException Erro de negócio
     */
    public Application updateApplication(Long idApplication, ApplicationRequest request) throws ApiBusinessException {

        verificarRegrasNegocio(request);

        if (idApplication == null || idApplication == 0)
            throw new ApiBusinessException("Identificador da aplicação não pode ser nulo ou zero.");

        var application = applicationRepository.findById(idApplication).orElse(null);
        if (application == null)
            throw new ApiBusinessException(String.format("Aplicação com o Id [%1s] não encontrado.", idApplication));

        if (!application.getApplicationName().equalsIgnoreCase(request.getApplicationName()))
        {//Verificar se na atualizar não está utilizando um nome já existente.
            var application_exist_name = applicationRepository.findByApplicationName(request.getApplicationName()).orElse(null);
            if (application_exist_name != null && !application_exist_name.getId().equals(idApplication))
                throw new ApiBusinessException(String.format("Existe uma aplicação [%1s] registrada com esse nome, não é permitido.", request.getApplicationName()));
        }

        application.setApplicationName(request.getApplicationName());
        application.setApplicationDescription(request.getApplicationDescription());
        application.setAllowedFileTypes(String.join(";", request.getAllowedFileTypes()));
        application.setMaximumFileSizeMB(request.getMaximumFileSizeMB());
        application.setAllowDuplicateFile(request.isAllowDuplicateFile());
        application.setCompressFileContent(request.isCompressFileContent());
        application.setApplyOcrFileContent(request.isApplyOcrFileContent());

        return applicationRepository.save(application);

    }

    /**
     * Aplicar a verificação das regras de negócio.
     * @param request - informações da aplicação.
     * @throws ApiBusinessException Erro de negócio
     */
    private static void verificarRegrasNegocio(ApplicationRequest request) throws ApiBusinessException {
        if (request == null)
            throw new ApiBusinessException("Informações da aplicação não pode ser nulo.");
        if (request.getApplicationName() == null || request.getApplicationName().isEmpty())
            throw new ApiBusinessException("Nome da aplicação não pode ser nulo ou vazio.");
        if (request.getAllowedFileTypes() == null || request.getAllowedFileTypes().length == 0)
            throw new ApiBusinessException("Tipos de arquivos (ContentType) não pode ser nulo ou vazio.");
        for (var contentType : request.getAllowedFileTypes()) {
            var valid = HelperFileType.isValidContentType(contentType);
            if (valid) continue;
            throw new ApiBusinessException(String.format("Tipos de arquivos (ContentType) [%1s] inválido.", contentType));
        }
        if (request.getMaximumFileSizeMB() == null || request.getMaximumFileSizeMB() < 1)
            throw new ApiBusinessException("Tamanho máximo dos arquivos (em MegaBytes) não pode ser menor ou igual a zero.");
    }

    /**
     * Lista as aplicações registradas.
     * @return Lista de apliações registradas.
     */
    public List<Application> getListApplication() {
        return applicationRepository.findAll();
    }

}