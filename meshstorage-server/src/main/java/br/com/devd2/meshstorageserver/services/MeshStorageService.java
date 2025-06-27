package br.com.devd2.meshstorageserver.services;

import br.com.devd2.meshstorageserver.entites.ServerStorage;
import br.com.devd2.meshstorageserver.exceptions.ApiBusinessException;
import br.com.devd2.meshstorageserver.models.enums.SystemStatusEnum;
import br.com.devd2.meshstorageserver.models.StatusMeshStorageModel;
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
            return statusMeshStorageModel;
        }
        long totalServerStorage = listServerStorages.size();
        long totalNotAvailable = listServerStorages.stream().filter(f -> !f.isAvailable()).count();
        long totalServerStorageAvailable = totalServerStorage - totalNotAvailable;

        if (totalServerStorageAvailable == 0) {
            statusMeshStorageModel.setSystemHealth(SystemStatusEnum.ERROR.getStatus());
            statusMeshStorageModel.setMessageStatus("Nenhum dos Server Storage está ativo para armazenamento.");
        } else if (totalServerStorageAvailable < totalServerStorage) {
            statusMeshStorageModel.setSystemHealth(SystemStatusEnum.WARNING.getStatus());
            statusMeshStorageModel.setMessageStatus("Existem alguns Server Storage desativados para armazenamento.");
        }
        else
        {
            statusMeshStorageModel.setSystemHealth(SystemStatusEnum.HEALTHY.getStatus());
            statusMeshStorageModel.setMessageStatus("Uffa! Sistema atualmente saudável e pronto para armazenamento.");
        }

        //Recuperar a informações de quantidades...
        long totalSpace = listServerStorages.stream().filter(ServerStorage::isAvailable).mapToLong(ServerStorage::getTotalSpace).sum();
        statusMeshStorageModel.setTotalSpaceStorages(totalSpace);
        long totalFree = listServerStorages.stream().filter(ServerStorage::isAvailable).mapToLong(ServerStorage::getFreeSpace).sum();
        statusMeshStorageModel.setTotalFreeStorages(totalFree);
        statusMeshStorageModel.setTotalClientsConnected(totalServerStorageAvailable);
        long totalFiles = listServerStorages.stream().mapToLong(ServerStorage::getTotalFiles).sum();
        statusMeshStorageModel.setTotalFilesStorages(totalFiles);

        return statusMeshStorageModel;

    }

}