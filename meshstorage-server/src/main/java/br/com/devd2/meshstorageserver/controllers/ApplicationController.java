package br.com.devd2.meshstorageserver.controllers;

import br.com.devd2.meshstorageserver.exceptions.ApiBusinessException;
import br.com.devd2.meshstorageserver.helper.HelperMapper;
import br.com.devd2.meshstorageserver.models.request.ApplicationRequest;
import br.com.devd2.meshstorageserver.models.response.ApplicationResponse;
import br.com.devd2.meshstorageserver.models.response.ErrorResponse;
import br.com.devd2.meshstorageserver.models.response.SuccessResponse;
import br.com.devd2.meshstorageserver.services.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/app")
@Tag(name = "Application", description = "Operações de gestão das aplicações que utilizam os servidores de armazenamento.")
public class ApplicationController {
    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Operation(summary = "Registrar Aplicação", description = "Registrar uma aplicação que irá utilizar o servidor de armazenamento de arquivos físicos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registro da aplicação realizada com sucesso", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos e regras de negócio", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro no servidor não tratado, requisição incorreta", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @PostMapping("/register")
    public ResponseEntity<?> registerApplication(@Valid @RequestBody ApplicationRequest request) {
        try {

            var application = applicationService.registerApplication(request);
            var response = HelperMapper.ConvertToResponse(application);
            return ResponseEntity.ok(response);

        } catch (ApiBusinessException error_business) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), error_business.getMessage()));
        } catch (Exception error) {
            var message = "Erro ao registrar uma aplicação para armazenamento de arquivos físicos.";
            log.error(message, error);
            return ResponseEntity.internalServerError().body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }

    }

    @Operation(summary = "Atualizar Aplicação", description = "Atualizar uma aplicação para armazenamento de arquivos físicos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atualizada aplicação com sucesso", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos e regras de negócio", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro no servidor não tratado, requisição incorreta", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateApplication(@PathVariable Long id, @Valid @RequestBody ApplicationRequest request) {

        try {

            var application = applicationService.updateApplication(id, request);
            var response = HelperMapper.ConvertToResponse(application);
            return ResponseEntity.ok(response);

        } catch (ApiBusinessException error_business) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), error_business.getMessage()));
        } catch (Exception error) {
            var message = "Erro ao atualizar aplicação para armazenamento de arquivos físicos.";
            log.error(message, error);
            return ResponseEntity.internalServerError().body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }

    }

    @Operation(summary = "Listar aplicações", description = "Lista todas as aplicações para armazenamento de arquivos físicos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista dos ServerStorage com sucesso", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationResponse[].class))}),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos e regras de negócio", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro no servidor não tratado, requisição incorreta", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @GetMapping("/list")
    public ResponseEntity<?> getListApplication() {
        try {
            var listApplication = applicationService.getListApplication();
            var response = HelperMapper.ConvertToResponseListApplication(listApplication);
            return ResponseEntity.ok(response);
        } catch (Exception error) {
            var message = "Erro ao listar as aplicações para armazenamento de arquivos físicos.";
            log.error(message, error);
            return ResponseEntity.internalServerError().body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }

    }

    @Operation(summary = "Remover Aplicação", description = "Remover (logicamente) uma aplicação do processo de armazemanto de arquivos físicos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Removida/desativada de aplicação com sucesso", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos e regras de negócio", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro no servidor não tratado, requisição incorreta", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @DeleteMapping("/remove/{id}")
    public ResponseEntity<?> removeApplication(@PathVariable Long id) {

        try {

            applicationService.removeApplication(id);
            return ResponseEntity.ok(new SuccessResponse("Aplicação removida com sucesso."));

        } catch (ApiBusinessException error_business) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), error_business.getMessage()));
        } catch (Exception error) {
            var message = "Erro ao atualizar aplicação para armazenamento de arquivos físicos.";
            log.error(message, error);
            return ResponseEntity.internalServerError().body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }

    }

}