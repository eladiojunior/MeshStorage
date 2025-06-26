package br.com.devd2.meshstorageserver.controllers;

import br.com.devd2.meshstorageserver.exceptions.ApiBusinessException;
import br.com.devd2.meshstorageserver.helper.HelperMapper;
import br.com.devd2.meshstorageserver.models.response.ErrorResponse;
import br.com.devd2.meshstorageserver.models.response.StatusMeshStorageResponse;
import br.com.devd2.meshstorageserver.services.MeshStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/system")
@Tag(name = "MeshStorage", description = "Operações do MeshStorage com um todo.")
public class SystemController {
    private final MeshStorageService meshStorageService;

    public SystemController(MeshStorageService meshStorageService) {
        this.meshStorageService = meshStorageService;
    }

    @Operation(summary = "Status do MeshStorage", description = "Verifica o status (saúde) e informações quantitativas do MeshStorage como um todo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status do ServerStorage com sucesso", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = StatusMeshStorageResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos e regras de negócio", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro no servidor não tratado, requisição incorreta", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @GetMapping("/status")
    public ResponseEntity<?> getSystemStatus() {
        try {
            var statusServerStorage = meshStorageService.statusMeshStorage();
            var response = HelperMapper.ConvertToResponseStatusMeshStorage(statusServerStorage);
            return ResponseEntity.ok(response);
        } catch (ApiBusinessException error_business) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), error_business.getMessage()));
        } catch (Exception error) {
            var message = "Erro ao verificar saúde do ServerStorage.";
            log.error(message, error);
            return ResponseEntity.internalServerError().body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }

    }
}
