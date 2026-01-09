package br.com.devd2.meshstorageserver.controllers;

import br.com.devd2.meshstorage.helper.FileUtil;
import br.com.devd2.meshstorageserver.entites.FileStorage;
import br.com.devd2.meshstorageserver.exceptions.ApiBusinessException;
import br.com.devd2.meshstorageserver.helper.HelperMapper;
import br.com.devd2.meshstorageserver.models.request.InitUploadRequest;
import br.com.devd2.meshstorageserver.models.response.*;
import br.com.devd2.meshstorageserver.services.FileStorageService;
import br.com.devd2.meshstorageserver.services.UploadChunkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/file")
@Tag(name = "FileStorage", description = "Operações de armazenamento de arquivos.")
public class FileStorageController {
    private final FileStorageService fileStorageService;
    private final UploadChunkService fileStorageUploadChunkService;

    public FileStorageController(FileStorageService fileStorageService,
                                 UploadChunkService fileStorageUploadChunkService) {
        this.fileStorageService = fileStorageService;
        this.fileStorageUploadChunkService = fileStorageUploadChunkService;
    }

    @Operation(summary = "Registrar um arquivo em um ServerStorage", description = "Registrar um arquivo no ServerStorage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registro do arquivo realizado com sucesso", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = FileStorageResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos e regras de negócio", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro no servidor não tratado, requisição incorreta", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @PostMapping(value="/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestParam("applicationCode")
                @Parameter(description = "Sigla do sistema responsável pelo upload.") String applicationCode,
            @RequestParam("file")
                @Parameter(description = "Informações do arquivo (bytes) para upload.") MultipartFile file) {

        try {

            var fileStorage = fileStorageService.registerFile(applicationCode, file);
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
    public ResponseEntity<?> downloadFile(@PathVariable
                                              @Parameter(description = "Identificador único do arquivo armazenado.") String idFile) {
        try {
            FileStorage file = fileStorageService.getFile(idFile);
            String contentType = file.getFileContentType();
            if (file.isCompressedFileContent() && file.getFileCompressed() != null)
                contentType = file.getFileCompressed().getCompressedFileContentType();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileFisicalName() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(file.getFileContent());
        } catch (ApiBusinessException error_business) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), error_business.getMessage()));
        } catch (Exception error) {
            var message = "Erro ao baixar um arquivo no Server Storage.";
            log.error(message, error);
            return ResponseEntity.internalServerError().body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }

    }
    @Operation(summary = "Baixar vários arquivos em ZIP", description = "Baixar vários arquivos (chave de acesso) de uma aplicação do ServerStorage em arquivo compactado .ZIP utilizando StreamResponseBody.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bytes do arquivo recuperado com sucesso", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Arrays.class))}),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos e regras de negócio", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro no servidor não tratado, requisição incorreta", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @GetMapping("/download/zip")
    public ResponseEntity<StreamingResponseBody> downloadZip(@RequestParam("idsFiles")
                                                                 @Parameter(description = "Lista de identificadores únicos do arquivo armazenado para recuperação do ZIP compactado.") List<String> idsFiles)  {
        try {

            List<String> uniqueIds = idsFiles.stream().distinct().toList();
            if (uniqueIds.isEmpty()) {
                log.info("Nenhum id de arquivo foi encontrado para download.");
                return ResponseEntity.badRequest().build();
            }

            String fisicalName = FileUtil.generatePhisicalNameByExtension(".zip");
            String zipName = "download_"+fisicalName;

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipName + "\"");
            headers.set(HttpHeaders.CACHE_CONTROL, "no-store");
            headers.set("X-Accel-Buffering", "no");

            StreamingResponseBody body = output -> {
                try (var zip = new java.util.zip.ZipOutputStream(output)) {
                    zip.setLevel(java.util.zip.Deflater.BEST_SPEED);
                    byte[] buf = new byte[8192];
                    for (String id : uniqueIds) {
                        try {
                            var file = fileStorageService.getFile(id);
                            var eZip = new java.util.zip.ZipEntry(file.getFileLogicName());
                            eZip.setSize(file.getFileContent().length);
                            zip.putNextEntry(eZip);
                            zip.write(file.getFileContent());
                            zip.closeEntry();
                            zip.flush();
                        } catch (ApiBusinessException error_business) {
                            var e = new java.util.zip.ZipEntry("error-" + id + ".txt");
                            zip.putNextEntry(e);
                            zip.write(error_business.getMessage().getBytes(StandardCharsets.UTF_8));
                            zip.closeEntry();
                        }
                    }
                }
            };
            return ResponseEntity.ok().headers(headers).body(body);
        } catch (Exception error) {
            var message = "Erro ao baixar arquivos compactados do Server Storage.";
            log.error(message, error);
            return ResponseEntity.internalServerError().build();
        }

    }

    @Operation(summary = "Remover arquivo do ServerStorage", description = "Remover um arquivo do ServerStorage pelo identificador do arquivo (chave de acesso).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Arquivo removido com sucesso", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseEntity.class))}),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos e regras de negócio", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro no servidor não tratado, requisição incorreta", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @DeleteMapping("/delete/{idFile}")
    public ResponseEntity<?> deleteFile (@PathVariable
                                             @Parameter(description = "Identificador único do arquivo armazenado.") String idFile) {
        try {
            var fileStorage = fileStorageService.deleteFile(idFile);
            var response = HelperMapper.ConvertToResponse(fileStorage);
            return ResponseEntity.ok(response);
        } catch (ApiBusinessException error_business) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), error_business.getMessage()));
        } catch (Exception error) {
            var message = "Erro ao remover um arquivo no Server Storage.";
            log.error(message, error);
            return ResponseEntity.internalServerError().body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }

    }

    @Operation(summary = "Lista de arquivos do ServerStorage", description = "Lista os arquivos de uma aplicação (sigla) de forma paginada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de arquivos recuperados da aplicação", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ListFileStorageResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos e regras de negócio", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro no servidor não tratado, requisição incorreta", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @GetMapping("/list")
    public ResponseEntity<?> listFiles (@RequestParam("applicationCode")
                                            @Parameter(description = "Sigla da aplicação responsável pelos arquivos") String applicationCode,
                                        @RequestParam(name = "pageNumber", defaultValue = "1")
                                            @Parameter(description = "Número da página da paginação") int pageNumber,
                                        @RequestParam(name = "recordsPerPage", defaultValue = "15")
                                            @Parameter(description = "Quantidade de registro que devem ser retornado por página na paginação") int recordsPerPage,
                                        @RequestParam(name = "isFilesSentForBackup", defaultValue = "false")
                                            @Parameter(description = "Filtro de arquivos já enviados para backup") boolean isFilesSentForBackup,
                                        @RequestParam(name = "isFilesRemoved", defaultValue = "false")
                                            @Parameter(description = "Filtro de arquivos removidos do armazenamento") boolean isFilesRemoved) {
        try {
            var list = fileStorageService.listFilesByApplicationCode(applicationCode, pageNumber, recordsPerPage, isFilesSentForBackup, isFilesRemoved);
            return ResponseEntity.ok(list);
        } catch (ApiBusinessException error_business) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), error_business.getMessage()));
        } catch (Exception error) {
            var message = "Erro ao listar os arquivos de uma aplicação de forma paginada no Server Storage.";
            log.error(message, error);
            return ResponseEntity.internalServerError().body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }

    }

    @Operation(summary = "Lista de status dos arquivos do ServerStorage", description = "Lista os codigos/descrições dos status arquivos do ServerSorage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de status dos arquivos", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Arrays.class))}),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos e regras de negócio", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro no servidor não tratado, requisição incorreta", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @GetMapping("/listStatusCode")
    public ResponseEntity<?> listStatusCode () {
        try {
            var list = fileStorageService.listStatusCodeFiles();
            return ResponseEntity.ok(list);
        } catch (Exception error) {
            var message = "Erro ao listar os status dos arquivos no Server Storage.";
            log.error(message, error);
            return ResponseEntity.internalServerError().body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }
    }

    @Operation(summary = "Lista de content types dos arquivos", description = "Lista os codigos/extensão/descrições/content_types dos arquivos do ServerSorage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de content types dos arquivos", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Arrays.class))}),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos e regras de negócio", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro no servidor não tratado, requisição incorreta", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @GetMapping("/listContentTypes")
    public ResponseEntity<?> listContentTypes () {
        try {
            var list = fileStorageService.listContentTypesFiles();
            return ResponseEntity.ok(list);
        } catch (Exception error) {
            var message = "Erro ao listar os content types dos arquivos no Server Storage.";
            log.error(message, error);
            return ResponseEntity.internalServerError().body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }
    }

    @Operation(summary = "Obtem informações de acesso ao arquivo do ServerStorage", description = "Obtem informações de link e imagem QR Code para acesso direto ao arquivo do ServerStorage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados de acesso ao arquivo do ServerStorage", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = QrCodeFileResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos e regras de negócio", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro no servidor não tratado, requisição incorreta", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @GetMapping("/qrcode/{idFile}")
    public ResponseEntity<?> qrCodeFile(@PathVariable
                                            @Parameter(description = "Identificador único do arquivo armazenado.") String idFile,
                                        @RequestParam(name="tokenExpirationTime", defaultValue="0")
                                            @Parameter(description = "Tempo de expiração do token de acesso (em minutos), se 0 nunca expira.") Long tokenExpirationTime,
                                        @RequestParam(name="maximumAccessestoken", defaultValue="0")
                                            @Parameter(description = "Quantidade máxima de acesso ao arquivo por token, se 0 sem limite.") int maximumAccessestoken) {
        try {
            var qrcode = fileStorageService.generateQrCode(idFile, tokenExpirationTime, maximumAccessestoken);
            return ResponseEntity.ok(qrcode);
        } catch (ApiBusinessException error_business) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), error_business.getMessage()));
        } catch (Exception error) {
            var message = "Erro ao gerar QR Code (imagem) de acesso ao arquivo no Server Storage.";
            log.error(message, error);
            return ResponseEntity.internalServerError().body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }
    }

    @Operation(summary = "Acesso, via token, arquivo do ServerStorage", description = "Baixa arquivo por link (QR Code) com verificação de token de acesso.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bytes do arquivo recuperado com sucesso", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Arrays.class))}),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos e regras de negócio", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro no servidor não tratado, requisição incorreta", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @GetMapping("/link/{token}")
    public ResponseEntity<?> downloadLinkFile(@PathVariable("token")
                                              @Parameter(description = "Token (chave acesso) ao arquivo para download") String token) {
        try {
            FileStorage file = fileStorageService.getFileByToken(token);
            String contentType = file.getFileContentType();
            if (file.isCompressedFileContent() && file.getFileCompressed() != null)
                contentType = file.getFileCompressed().getCompressedFileContentType();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileFisicalName() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(file.getFileContent());
        } catch (ApiBusinessException error_business) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), error_business.getMessage()));
        } catch (Exception error) {
            var message = "Erro ao baixar um arquivo no Server Storage.";
            log.error(message, error);
            return ResponseEntity.internalServerError().body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }

    }

    @Operation(summary = "Inicia upload de arquivo em bloco", description = "Inicia upload de arquivo em blocos para armazenamento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registro do processo de upload em bloco", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = InitUploadResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos e regras de negócio", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro no servidor não tratado, requisição incorreta", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @PostMapping(path = "/uploadInChunk/init", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> uploadInitFile(@RequestBody @Valid InitUploadRequest request) {
        try {
            var result = fileStorageUploadChunkService.initUpload(request);
            return ResponseEntity.ok(result);
        } catch (ApiBusinessException error_business) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), error_business.getMessage()));
        } catch (Exception error) {
            var message = "Erro ao iniciar upload enviando blocos do arquivo para o Server Storage.";
            log.error(message, error);
            return ResponseEntity.internalServerError().body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }
    }

    @Operation(summary = "Envio de bloco do arquivo", description = "Envio do bloco do arquivo para armazenamento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bloco do upload do arquivo", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos e regras de negócio", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro no servidor não tratado, requisição incorreta", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @PutMapping(path = "/uploadInChunk/chunk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadChunkFile(
            @RequestParam(name="uploadId")
                @Parameter(description = "Identificador do upload em andamento.") String uploadId,
            @RequestParam("chunkIndex")
                @Parameter(description = "Index do bloco do upload em andamento.") int chunkIndex,
            @RequestParam("chunkTotal")
                @Parameter(description = "Total de blocos do upload em andamento.") long chunkTotal,
            @RequestPart("chunkBlob")
                @Parameter(description = "Informações do bloco do arquivo (bytes) do upload em andamento.") MultipartFile chunkBlob
    ) {

        try (var in = chunkBlob.getInputStream()) {
            fileStorageUploadChunkService.receiveChunk(uploadId, chunkIndex, chunkTotal, in, chunkBlob.getSize());
            return ResponseEntity.ok(new SuccessResponse("Bloco "+chunkIndex+" de "+chunkTotal+" recebido com sucesso."));
        } catch (ApiBusinessException error_business) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), error_business.getMessage()));
        } catch (Exception error) {
            var message = "Erro ao receber bloco do upload do arquivo.";
            log.error(message, error);
            return ResponseEntity.internalServerError().body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }

    }

    @Operation(summary = "Finaliza upload em bloco do arquivo", description = "Finaliza o processo de upload do arquivo em bloco para armazenamento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bloco do upload do arquivo", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = FinalizeUploadResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos e regras de negócio", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro no servidor não tratado, requisição incorreta", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @PostMapping(path = "/uploadInChunk/finalize/{uploadId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> uploadFinalizeFile(@PathVariable("uploadId")
                                                @Parameter(description = "Identificador único do upload para finalização.") String uploadId) {
        try {
            var response = fileStorageUploadChunkService.finalizeUpload(uploadId);
            return ResponseEntity.ok(response);
        } catch (ApiBusinessException error_business) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), error_business.getMessage()));
        } catch (Exception error) {
            var message = "Erro ao finalizar upload em blocos do arquivo para o Server Storage.";
            log.error(message, error);
            return ResponseEntity.internalServerError().body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }
    }

    @Operation(summary = "Cancelar upload em bloco do arquivo", description = "Cancelar o processo de upload do arquivo em bloco para armazenamento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cancelar upload do arquivo", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Parametros inválidos e regras de negócio", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro no servidor não tratado, requisição incorreta", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @PostMapping(path = "/uploadInChunk/cancel/{uploadId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> uploadCancelFile(@PathVariable("uploadId")
                                                @Parameter(description = "Identificador único do upload para cancelamento.") String uploadId) {
        try {
            fileStorageUploadChunkService.cancelUpload(uploadId);
            return ResponseEntity.ok(new SuccessResponse("Upload "+uploadId+" cancelado com sucesso."));
        } catch (ApiBusinessException error_business) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), error_business.getMessage()));
        } catch (Exception error) {
            var message = "Erro ao cancelar upload em blocos do arquivo para o Server Storage.";
            log.error(message, error);
            return ResponseEntity.internalServerError().body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
        }
    }

}