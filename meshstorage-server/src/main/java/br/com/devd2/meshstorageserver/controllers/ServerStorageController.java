package br.com.devd2.meshstorageserver.controllers;

import br.com.devd2.meshstorageserver.exceptions.ApiBusinessException;
import br.com.devd2.meshstorageserver.helper.HelperMapper;
import br.com.devd2.meshstorageserver.models.response.ErrorResponse;
import br.com.devd2.meshstorageserver.models.response.ServerStorageResponse;
import br.com.devd2.meshstorageserver.models.response.StatusMeshStorageResponse;
import br.com.devd2.meshstorageserver.services.ServerStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/server")
@Tag(name = "ServerStorage", description = "Operações de gestão dos servidores de armazenamento e configurações.")
public class ServerStorageController {
    private final ServerStorageService serverStorageService;

    public ServerStorageController(ServerStorageService serverStorageService) {
        this.serverStorageService = serverStorageService;
    }

    @Operation(summary = "Melhor ServerStorage", description = "Obter o melhor Server Storage para armazenamento de arquivos físicos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtem melhor ServerStorage com sucesso", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ServerStorageResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos e regras de negócio", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro no servidor não tratado, requisição incorreta", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @GetMapping("/best")
    public ResponseEntity<?> getBestServerStorage() {

        try {

            var serverStorage = serverStorageService.getBestServerStorage();
            var response = HelperMapper.ConvertToResponse(serverStorage);
            return ResponseEntity.ok(response);

        } catch (ApiBusinessException error_business) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), error_business.getMessage()));
        } catch (Exception error) {
            var message = "Erro ao obter melhor ServerStorage para armazenamento de arquivos físicos.";
            log.error(message, error);
            return ResponseEntity.internalServerError().body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }

    }

    @Operation(summary = "Lista os ServerStorage", description = "Lista todos os Server Storages para armazenamento de arquivos físicos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista dos ServerStorage com sucesso", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ServerStorageResponse[].class))}),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos e regras de negócio", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro no servidor não tratado, requisição incorreta", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @GetMapping("/list")
    public ResponseEntity<?> getListServerStorage(
            @RequestParam(name = "available", defaultValue = "true")
                @Parameter(description = "Flag de disponibilidade do storage para utilização") boolean available) {
        try {
            var listServerStorage = serverStorageService.getListServerStorage(available);
            var response = HelperMapper.ConvertToResponseListServerStorage(listServerStorage);
            return ResponseEntity.ok(response);
        } catch (ApiBusinessException error_business) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), error_business.getMessage()));
        } catch (Exception error) {
            var message = "Erro ao listar os ServerStorage para armazenamento de arquivos físicos.";
            log.error(message, error);
            return ResponseEntity.internalServerError().body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }

    }

}