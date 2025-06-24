package br.com.devd2.meshstorageserver.services;

import br.com.devd2.meshstorageserver.exceptions.ApiBusinessException;
import br.com.devd2.meshstorageserver.models.enums.SystemStatusEnum;
import br.com.devd2.meshstorageserver.models.response.StatusMeshStorageModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MeshStorageService {
    private final ServerStorageService serverStorageService;

    public MeshStorageService(ServerStorageService serverStorageService) {
        this.serverStorageService = serverStorageService;
    }

    /**
     * Verifica a saúde do MeshStorage para reportar seu status atual.
     * @return Informações da saúde do MeshStorage.
     */
    public StatusMeshStorageModel statusMeshStorage() throws ApiBusinessException {
        StatusMeshStorageModel statusMeshStorageModel = new StatusMeshStorageModel();
        statusMeshStorageModel.setDateTimeAvailable(LocalDateTime.now());
        var listServerStorages = serverStorageService.getListServerStorage(false);
        if (listServerStorages.isEmpty()) {
            statusMeshStorageModel.setSystemHealth(SystemStatusEnum.ERROR.getStatus());
            statusMeshStorageModel.setMessageStatus("Não identificamos nenhum Server Storage configurado.");
        }
        else if (listServerStorages.stream().map(f -> !f.isAvailable()).count() > 1) {
            statusMeshStorageModel.setSystemHealth(SystemStatusEnum.WARNING.getStatus());
            statusMeshStorageModel.setMessageStatus("Existem alguns Server Storage desativados.");
        }
        else {
            statusMeshStorageModel.setSystemHealth(SystemStatusEnum.HEALTHY.getStatus());
            statusMeshStorageModel.setMessageStatus("Uffa! Sistema atualmente saudável.");
        }
        return statusMeshStorageModel;
    }

}