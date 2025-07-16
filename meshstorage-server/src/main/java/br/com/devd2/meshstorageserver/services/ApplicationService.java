package br.com.devd2.meshstorageserver.services;

import br.com.devd2.meshstorageserver.entites.Application;
import br.com.devd2.meshstorageserver.exceptions.ApiBusinessException;
import br.com.devd2.meshstorageserver.helper.HelperFormat;
import br.com.devd2.meshstorageserver.helper.HelperFileType;
import br.com.devd2.meshstorageserver.models.enums.ApplicationStatusEnum;
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
        application.setCompressedFileContentToZip(request.isCompressedFileContentToZip());
        application.setConvertImageFileToWebp(request.isConvertImageFileToWebp());
        application.setTotalFiles(0L);
        application.setDateTimeRegisteredApplication(LocalDateTime.now());
        application.setApplicationStatusCode(ApplicationStatusEnum.ACTIVE.getCode());

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
        application.setCompressedFileContentToZip(request.isCompressedFileContentToZip());
        application.setConvertImageFileToWebp(request.isConvertImageFileToWebp());
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


    /**
     * Atualizar a quantidade de arquivo registrado na Aplicação, upload.
     * @param idAplicacao - Identificador da Aplicação para atualizar a quantidade;
     * @param hasAdicionar - flag para que indica se será para adicionar (true) ou subtrair (false) da quantidade.
     * @throws ApiBusinessException - Erro de negócio
     */
    public void updateApplicationTotalFile(Long idAplicacao, boolean hasAdicionar) throws ApiBusinessException {

        if (idAplicacao == null || idAplicacao == 0)
            throw new ApiBusinessException("Identificador da Aplicação não pode ser nulo ou zero.");

        //Verificar Application existente para atualização.
        Application application = applicationRepository.findById(idAplicacao).orElse(null);
        if (application == null)
            throw new ApiBusinessException("Aplicação não identificada para atualização da quantidade de arquivos.");

        long totalFiles = application.getTotalFiles() == null ? 0 : application.getTotalFiles();
        totalFiles = hasAdicionar ? totalFiles + 1 : totalFiles - 1;
        if (totalFiles < 0) totalFiles = 0; //Evitar informação negativa;
        application.setTotalFiles(totalFiles);

        applicationRepository.save(application);

    }

    /**
     * Recupera uma aplicação pelo seu nome.
     * @param applicationName - Nome da aplicação a ser recuperada.
     * @return Objeto Application carregado ou nulo se não encontrar.
     */
    public Application getApplicationByName(String applicationName) throws ApiBusinessException {

        if (applicationName == null || applicationName.isEmpty())
            throw new ApiBusinessException("Nome da Aplicação não pode ser nulo ou vazia.");

        return applicationRepository.findByApplicationName(applicationName).orElse(null);

    }

    /**
     * Remove uma aplicação da estrutura de armazenamento, uma remoção lógica.
     * @param idAplicacao - Identificação da aplicação para remoção ou desativação.
     */
    public void removeApplication(Long idAplicacao) throws ApiBusinessException {

        if (idAplicacao == null || idAplicacao == 0)
            throw new ApiBusinessException("Identificador da Aplicação não pode ser nulo ou zero.");

        //Verificar Application existente para remoção.
        Application application = applicationRepository.findById(idAplicacao).orElse(null);
        if (application == null)
            throw new ApiBusinessException("Aplicação não identificada para remoção.");
        if (application.getApplicationStatusCode() == ApplicationStatusEnum.REMOVED.getCode())
            throw new ApiBusinessException(String.format("Aplicação já foi removida (logicamente) em [%1s].",
                    HelperFormat.formatDateTime(application.getDateTimeRemovedApplication(), "dd/MM/yyyy HH:mm:ss")));

        application.setApplicationStatusCode(ApplicationStatusEnum.REMOVED.getCode());
        application.setDateTimeRemovedApplication(LocalDateTime.now());

        applicationRepository.save(application);

    }

}