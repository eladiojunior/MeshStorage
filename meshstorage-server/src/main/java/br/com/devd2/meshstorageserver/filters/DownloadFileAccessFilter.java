package br.com.devd2.meshstorageserver.filters;

import br.com.devd2.meshstorageserver.entites.FileStorage;
import br.com.devd2.meshstorageserver.entites.FileAccessToken;
import br.com.devd2.meshstorageserver.entites.FileLogAccess;
import br.com.devd2.meshstorageserver.models.UserAccessModel;
import br.com.devd2.meshstorageserver.repositories.FileStorageAccessTokenRepository;
import br.com.devd2.meshstorageserver.repositories.FileStorageLogAccessRepository;
import br.com.devd2.meshstorageserver.repositories.FileStorageRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
public class DownloadFileAccessFilter extends OncePerRequestFilter {
    private final FileStorageLogAccessRepository fileStorageLogAccessRepository;
    private final FileStorageRepository fileStorageRepository;
    private final FileStorageAccessTokenRepository fileStorageAccessTokenRepository;

    public DownloadFileAccessFilter(FileStorageLogAccessRepository fileStorageLogAccessRepository, FileStorageRepository fileStorageRepository, FileStorageAccessTokenRepository fileStorageAccessTokenRepository) {
        this.fileStorageLogAccessRepository = fileStorageLogAccessRepository;
        this.fileStorageRepository = fileStorageRepository;
        this.fileStorageAccessTokenRepository = fileStorageAccessTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        chain.doFilter(request, response);   // processa primeiro → status code disponível

        if (response.getStatus() == 200) {


            FileStorage fileStorage = null;
            String userName = null;
            if (request.getRequestURI().startsWith("/api/v1/file/download/")) {
                String idFile = request.getRequestURI().replace("/api/v1/file/download/", "").trim();
                fileStorage = fileStorageRepository.findByIdFile(idFile).orElse(null);
            } else if (request.getRequestURI().startsWith("/api/v1/file/link/")) {
                String token = request.getRequestURI().replace("/api/v1/file/link/", "").trim();
                FileAccessToken accessToken = fileStorageAccessTokenRepository
                        .findByAccessToken(token).orElse(null);
                if (accessToken != null)
                    fileStorage = fileStorageRepository.findByIdFile(accessToken.getIdFile()).orElse(null);
                //No caso de accesso via Token, registrar o usuário como sendo o Token.
                userName = token;
            }

            if (fileStorage == null)
                return;

            if (userName == null || userName.isEmpty()) {
                userName = Optional.ofNullable(request.getHeader("X-User-Name"))
                        .orElseGet(() -> request.getUserPrincipal() != null
                                ? request.getUserPrincipal().getName() : "anonymous");
            }
            String ip = extractClientIp(request);
            String channel = Optional.ofNullable(request.getHeader("X-Access-Channel")).orElse("unknown");
            String userAgent = request.getHeader("User-Agent");

            registerFileAccessHistory(fileStorage, new UserAccessModel(userName, ip, userAgent, channel));

        }
    }

    /**
     * Recupera o IP da requisição.
     * @param req - Requisição da API
     * @return IP extraído da requisição.
     */
    private String extractClientIp(HttpServletRequest req) {
        String fwd = req.getHeader("X-Forwarded-For");
        return fwd != null ? fwd.split(",")[0].trim() : req.getRemoteAddr();
    }

    /**
     * Resposável por registrar as informações de acesso ao arquivo no histórico.
     * Não teve afetar o processamento de recuperação do arquivo, caso ocorra erro deve registrar e
     * seguir com o processamento.
     * @param fileStorage - Informações do arquivo acessado.
     * @param user - Informações do usuário que está acessando o arquivo.
     */
    private void registerFileAccessHistory(FileStorage fileStorage, UserAccessModel user) {

        try {

            FileLogAccess fileStorageAccessLog = new FileLogAccess();
            fileStorageAccessLog.setFileStorage(fileStorage);
            if (user != null) {
                fileStorageAccessLog.setUserName(user.getUserName());
                fileStorageAccessLog.setIpUser(user.getIpUser());
                fileStorageAccessLog.setUserAgent(user.getUserAgent());
                fileStorageAccessLog.setAccessChanel(user.getAccessChanel());
            }
            fileStorageAccessLog.setDateTimeRegisteredAccess(LocalDateTime.now());

            fileStorageLogAccessRepository.saveAsync(fileStorageAccessLog);

        } catch (Exception error) {
            log.error("Erro ao registrar acesso ao arquivo.", error);
        }

    }

}