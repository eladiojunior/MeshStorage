package br.com.devd2.meshstorageserver.helper;

import br.com.devd2.meshstorage.enums.ExtractionTextByOcrStatusEnum;
import br.com.devd2.meshstorage.enums.FileStorageStatusEnum;
import br.com.devd2.meshstorageserver.entites.*;
import br.com.devd2.meshstorageserver.models.StatusMeshStorageModel;
import br.com.devd2.meshstorageserver.models.enums.ServerStorageStatusEnum;
import br.com.devd2.meshstorageserver.models.response.*;

import java.util.List;
import java.util.stream.Collectors;

public class HelperMapper {

    /**
     * Converte um objeto Entity (ServerStorage) para Response (ServerStorageResponse).
     *
     * @param serverStorage - Objeto a ser convertido em Response
     * @return Instancia de Respose
     */
    public static ServerStorageResponse ConvertToResponse(ServerStorage serverStorage) {
        if (serverStorage == null) {
            return null;
        }
        ServerStorageResponse response = new ServerStorageResponse();
        response.setId(serverStorage.getId());
        response.setIdClient(serverStorage.getIdServerStorageClient());
        response.setServerName(serverStorage.getServerName());
        response.setStorageName(serverStorage.getStorageName());
        response.setIpServer(serverStorage.getIpServer());
        response.setOsServer(serverStorage.getOsServer());
        response.setFreeSpace(serverStorage.getMetrics().getFreeSpace());
        response.setTotalSpace(serverStorage.getMetrics().getTotalSpace());
        response.setTotalFiles(serverStorage.getMetrics().getTotalFiles());
        response.setStatusCode(serverStorage.getServerStorageStatusCode());
        response.setStatusDescription(ServerStorageStatusEnum.getValue(serverStorage.getServerStorageStatusCode()));
        response.setDateTimeRegistered(serverStorage.getDateTimeRegisteredServerStorage());
        response.setDateTimeRemoved(serverStorage.getDateTimeRemovedServerStorage());
        if (serverStorage.getMetrics() != null)
            response.setMetrics(ConvertToResponse(serverStorage.getMetrics()));
        return response;
    }

    /**
     * Converte um objeto Entity (ServerStorageMetrics) para Response (ServerStorageMetricsResponse).
     *
     * @param metrics - Objeto a ser convertido em Response
     * @return Instancia de Respose
     */
    public static ServerStorageMetricsResponse ConvertToResponse(ServerStorageMetrics metrics) {
        if (metrics == null) {
            return null;
        }
        ServerStorageMetricsResponse response = new ServerStorageMetricsResponse();
        response.setTotalSpace(metrics.getTotalSpace());
        response.setFreeSpace(metrics.getFreeSpace());
        response.setResponseTime(metrics.getResponseTime());
        response.setRequestLastMinute(metrics.getRequestLastMinute());
        response.setDateTimeLastRequest(metrics.getDateTimeLastRequest());
        response.setErrosLastRequest(metrics.getErrosLastRequest());
        response.setDateTimeLastAvailable(metrics.getDateTimeLastAvailable());
        return response;
    }

    /**
     * Converte um objeto Entity (FileStorage) para Response (FileStorageResponse).
     *
     * @param fileStorage - Objeto a ser convertido em Response
     * @return Instancia de Response
     */
    public static FileStorageResponse ConvertToResponse(FileStorage fileStorage) {
        if (fileStorage == null) {
            return null;
        }
        FileStorageResponse response = new FileStorageResponse();
        response.setIdFile(fileStorage.getIdFile());
        response.setFileLogicName(fileStorage.getFileLogicName());
        response.setFileFisicalName(fileStorage.getFileFisicalName());
        response.setFileContentType(fileStorage.getFileContentType());
        response.setFileLength(fileStorage.getFileLength());
        response.setHashFileBytes(fileStorage.getHashFileBytes());
        response.setExtractionTextFileByOcr(fileStorage.isExtractionTextFileByOcr());
        if (fileStorage.isExtractionTextFileByOcr() && fileStorage.getFileExtractionByOcr() != null)
            response.setFileExtractionByOcr(ConvertToResponse(fileStorage.getFileExtractionByOcr()));
        response.setCompressedFileContent(fileStorage.isCompressedFileContent());
        if (fileStorage.isCompressedFileContent() && fileStorage.getFileCompressed() != null)
            response.setFileCompressed(ConvertToResponse(fileStorage.getFileCompressed()));
        response.setDateTimeRegisteredFileStorage(fileStorage.getDateTimeRegisteredFileStorage());
        response.setDateTimeRemovedFileStorage(fileStorage.getDateTimeRemovedFileStorage());
        response.setDateTimeBackupFileStorage(fileStorage.getDateTimeBackupFileStorage());
        response.setFileStatusCode(fileStorage.getFileStatusCode());
        response.setFileStatusDescription(FileStorageStatusEnum.getValue(fileStorage.getFileStatusCode()));
        return response;
    }

    /**
     * Converte um objeto Entity (FileStorageCompressed) para Response (FileStorageCompressedResponse).
     *
     * @param fileStorageCompressed - Objeto a ser convertido em Response
     * @return Instancia de Response
     */
    public static FileStorageCompressedResponse ConvertToResponse(FileStorageCompressed fileStorageCompressed) {
        if (fileStorageCompressed == null) {
            return null;
        }
        FileStorageCompressedResponse response = new FileStorageCompressedResponse();
        response.setCompressedFileLength(fileStorageCompressed.getCompressedFileLength());
        response.setCompressedFileContentType(fileStorageCompressed.getCompressedFileContentType());
        response.setCompressedFileInformation(fileStorageCompressed.getCompressionFileInformation());
        response.setCompressedHashFileBytes(fileStorageCompressed.getCompressedHashFileBytes());
        response.setPercentualCompressedFile(fileStorageCompressed.getPercentualCompressedFileContent());
        return response;
    }

    /**
     * Converte um objeto Entity (FileOcrExtraction) para Response (FileOcrExtractionResponse).
     *
     * @param fileOcrExtraction - Objeto a ser convertido em Response
     * @return Instancia de Response
     */
    public static FileOcrExtractionResponse ConvertToResponse(FileOcrExtraction fileOcrExtraction) {
        if (fileOcrExtraction == null) {
            return null;
        }
        FileOcrExtractionResponse response = new FileOcrExtractionResponse();
        response.setContentTextByOcr(fileOcrExtraction.getTextFileOcr());
        response.setHashContentTextByOcr(fileOcrExtraction.getHashContentFile());
        response.setNameTypeDocumentByOcr(fileOcrExtraction.getDocumentType());
        response.setPercentualConfidenceTypeDocumentByOcr(fileOcrExtraction.getDegreeConfidenceDocumentType());
        response.setCodeStatusExtractionTextByOrc(fileOcrExtraction.getExtractionTextByOrcStatusCode());
        response.setDescriptionStatusExtractionTextByOrc(
                ExtractionTextByOcrStatusEnum.getValue(fileOcrExtraction.getExtractionTextByOrcStatusCode()));
        response.setDateTimeStartExtractionByOcr(fileOcrExtraction.getDateTimeStartExtraction());
        response.setDateTimeEndExtractionByOcr(fileOcrExtraction.getDateTimeEndExtraction());
        if (fileOcrExtraction.getFieldsOcrExtraction() != null &&
                !fileOcrExtraction.getFieldsOcrExtraction().isEmpty()) {
            response.setFieldsExtractionByOcr(ConvertToResponseListFieldsOcrExtraction
                    (fileOcrExtraction.getFieldsOcrExtraction()));
        }
        return response;
    }

    /**
     * Converte um objeto Entity (FileOcrExtractionFields) para Response (FileOcrExtractionFieldsResponse).
     *
     * @param fileOcrExtractionFields - Objeto a ser convertido em Response
     * @return Instancia de Response
     */
    private static FileOcrExtractionFieldsResponse ConvertToResponse(FileOcrExtractionFields fileOcrExtractionFields) {
        if (fileOcrExtractionFields == null) {
            return null;
        }
        FileOcrExtractionFieldsResponse response = new FileOcrExtractionFieldsResponse();
        response.setKeyField(fileOcrExtractionFields.getKeyField());
        response.setValueField(fileOcrExtractionFields.getValueField());
        return response;
    }


    /**
     * Converte lista de objeto Entity (FileOcrExtractionFields) para lista de Response (FileOcrExtractionFieldsResponse).
     * @param listFieldsExtractionOcr - Lista de Entites para conversão.
     * @return Lista de Responses convertidas.
     */
    public static List<FileOcrExtractionFieldsResponse> ConvertToResponseListFieldsOcrExtraction
    (List<FileOcrExtractionFields> listFieldsExtractionOcr) {
        return listFieldsExtractionOcr.stream().map(HelperMapper::ConvertToResponse).toList();
    }

    /**
     * Converte lista de objeto Entity (ServerStorage) para lista de Response (ServerStorageResponse).
     * @param listServerStorage - Lista de Entites para conversão.
     * @return Lista de Responses convertidas.
     */
    public static List<ServerStorageResponse> ConvertToResponseListServerStorage
    (List<ServerStorage> listServerStorage) {
        return listServerStorage.stream().map(HelperMapper::ConvertToResponse).toList();
    }

    /**
     * Converte um objeto Entity (Application) para Response (ApplicationResponse).
     *
     * @param application - Objeto a ser convertido em Response
     * @return Instancia de Response
     */
    public static ApplicationResponse ConvertToResponse(Application application) {
        if (application == null)
            return null;
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setApplicationCode(application.getApplicationCode());
        response.setApplicationDescription(application.getApplicationDescription());
        response.setMaximumFileSize(application.getMaximumFileSizeMB());
        response.setAllowedFileTypes(application.getAllowedFileTypes().split(";"));
        response.setAllowDuplicateFile(application.isAllowDuplicateFile());
        response.setRequiresFileReplication(application.isRequiresFileReplication());
        response.setCompressedFileContentToZip(application.isCompressedFileContentToZip());
        response.setConvertImageFileToWebp(application.isConvertImageFileToWebp());
        response.setApplyOcrFileContent(application.isApplyOcrFileContent());
        response.setTotalFiles(application.getTotalFiles());
        response.setDateTimeApplication(application.getDateTimeRegisteredApplication());
        return response;
    }

    /**
     * Converte lista de objeto Entity (Application) para lista de Response (ApplicationResponse).
     * @param list - Lista de Entites para conversão.
     * @return Lista de Responses convertidas.
     */
    public static List<ApplicationResponse> ConvertToResponseListApplication(List<Application> list) {
        return list.stream().map(HelperMapper::ConvertToResponse).collect(Collectors.toList());
    }

    /**
     * Converte lista de objeto Entity (FileStorage) para lista de Response (FileStorageResponse).
     * @param list - Lista de Entites para conversão.
     * @return Lista de Responses convertidas.
     */
    public static List<FileStorageResponse> ConvertToResponseListFileStorage(List<FileStorage> list) {
        return list.stream().map(HelperMapper::ConvertToResponse).collect(Collectors.toList());
    }

    /**
     * Converte uma Model em Response (StatusMeshStorageResponse) para retornar na API;
     * @param model - Informações do status.
     * @return Objeto de Response carregad.
     */
    public static StatusMeshStorageResponse ConvertToResponseStatusMeshStorage(StatusMeshStorageModel model) {
        if (model == null)
            return null;
        StatusMeshStorageResponse response = new StatusMeshStorageResponse();
        response.setSystemHealth(model.getSystemHealth());
        response.setMessageStatus(model.getMessageStatus());
        response.setTotalSpaceStorages(model.getTotalSpaceStorages());
        response.setTotalFreeStorages(model.getTotalFreeStorages());
        response.setTotalClientsConnected(model.getTotalClientsConnected());
        response.setTotalFilesStorages(model.getTotalFilesStorages());
        response.setDateTimeAvailable(model.getDateTimeAvailable());
        return response;
    }
}