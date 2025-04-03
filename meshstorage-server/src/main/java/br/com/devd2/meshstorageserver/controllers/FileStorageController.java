package br.com.devd2.meshstorageserver.controllers;

import br.com.devd2.meshstorageserver.entites.FileStorage;
import br.com.devd2.meshstorageserver.exceptions.ApiBusinessException;
import br.com.devd2.meshstorageserver.helper.HelperMapper;
import br.com.devd2.meshstorageserver.models.response.ErrorResponse;
import br.com.devd2.meshstorageserver.models.response.FileStorageResponse;
import br.com.devd2.meshstorageserver.services.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping("/api/file")
@Tag(name = "FileStorage", description = "Operações de armazenamento de arquivos.")
public class FileStorageController {
    private final FileStorageService fileStorageService;

    public FileStorageController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Operation(summary = "Registrar um arquivo em um ServerStorage", description = "Registrar um arquivo no ServerStorage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registro do arquivo realizado com sucesso", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = FileStorageResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos e regras de negócio", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro no servidor não tratado, requisição incorreta", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @PostMapping("/upload")
    public ResponseEntity uploadFile(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("file") MultipartFile file) {

        try {

            var fileStorage = fileStorageService.registerFile(applicationName, file);
            var response = HelperMapper.ConvertToResponse(fileStorage);
            return ResponseEntity.ok(response);

        } catch (ApiBusinessException error_business) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), error_business.getMessage()));
        } catch (Exception error) {
            var message = "Erro ao registrar um arquivo no Server Storage.";
            log.error(message, error);
            return ResponseEntity.internalServerError().body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }

    }

    @Operation(summary = "Baixar arquivo do ServerStorage", description = "Baixa um arquivo do ServerStorage pelo identificador do arquivo (chave de acesso).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bytes do arquivo recuperado com sucesso", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Arrays.class))}),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos e regras de negócio", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro no servidor não tratado, requisição incorreta", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @GetMapping("/download/{idFile}")
    public ResponseEntity download(
            @PathVariable String idFile) {
        try {
            FileStorage file = fileStorageService.getFile(idFile);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileLogicName() + "\"")
                    .body(file.getFileData());
        } catch (ApiBusinessException error_business) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), error_business.getMessage()));
        } catch (Exception error) {
            var message = "Erro ao registrar um arquivo no Server Storage.";
            log.error(message, error);
            return ResponseEntity.internalServerError().body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }

    }

}