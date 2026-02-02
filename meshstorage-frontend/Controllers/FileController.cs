using meshstorage_frontend.Helper;
using meshstorage_frontend.Models.Dto;
using meshstorage_frontend.Models.ViewModels;
using meshstorage_frontend.Services;
using meshstorage_frontend.Services.Exceptions;
using Microsoft.AspNetCore.Mvc;

namespace meshstorage_frontend.Controllers;

public class FileController(
    IApiService apiService,
    RazorViewToStringRenderer renderer,
    ILogger<FileController> logger)
    : DefaultController(renderer)
{
    
    // GET File/SearchFile
    [HttpGet]
    public IActionResult SearchFile(string codeApplication)
    {
        
        if (string.IsNullOrEmpty(codeApplication))
            return RedirectToActionByMessage("Index", "Dashboard",
                true, "Sigla da aplicação não informada.");

        var filterSession = new FilterListFileDto
        {
            ApplicationCode = codeApplication,
            Page = 1,
            PageSize = 15
        };

        var model = apiService.ListFilesFilter(filterSession).Result;
        return View(model);
        
    }

    // POST File/SearchFileBuFilter
    [HttpPost]
    public IActionResult SearchFileByFilter(FilterListFileViewModel filter, int? page, int? pageSize)
    {
        
        var sessionFilter = new FilterListFileDto()
        {
            ApplicationCode = filter.ApplicationCode,
            FileLogicName = filter.FileLogicName??string.Empty,
            FileContentType = (filter.FileContentType!=null?filter.FileContentType.Split(";"): []),
            FilesRemoved = filter.FilesRemoved,
            FilesSentForBackup = filter.FilesSentForBackup,
            Page = page ?? 1,
            PageSize = pageSize ?? 15,
            LastUpdated = DateTime.UtcNow
        };
        
        HttpContext.Session.SetObject(KeySessionEnum.FilterListFile.GetDescription(), sessionFilter);
        
        var model = apiService.ListFilesFilter(sessionFilter).Result;
        return View("SearchFile", model);
        
    }

    // GET File/DownloadFile
    [HttpGet]
    public async Task<IActionResult> DownloadFile(string idFile)
    {
        try
        {
            var resultDownload = await apiService.DownloadFile(idFile, "eladio.junior", "Site");
            return File(resultDownload.Stream, resultDownload.ContentType, resultDownload.FileName);
        }
        catch (ApiBusinessException error)
        {
            var modelError = new ErrorViewModel(error.Code.ToString(), error.Message);
            return View("Error", modelError);
        }
        
    } 
    
    // DELETE File/RemoveFile
    [HttpDelete]
    public IActionResult JsonRemoveFile(string idFile)
    {
        try
        {
            apiService.RemoveFile(idFile);
            return JsonResultSucesso("Arquivo ["+idFile+"] removido com sucesso");
        }
        catch (Exception error)
        {
            logger.LogError(error, "RemoveFile(string::idFile)");
            return TratarErroNegocio(error, $"RemoveFile(idFile={idFile})");
        }
        
    }
    
    // POST File/GenerateQrCodeFile
    [HttpPost]
    public IActionResult JsonGenerateQrCodeFile([FromBody] GenerateQrCodeDto request)
    {
        try
        {

            var model = apiService.GenerateLinkQrCodeFile(request.IdFile,
                request.TokenExpirationTime, request.MaximumAccessesToken).Result;
            if (model == null)
                return JsonResultErro("Não foi possível gerar o QrCode e Link de acesso ao arquivo.");
            
            model.TokenExpirationTime = request.TokenExpirationTime;
            model.MaximumAccessesToken = request.MaximumAccessesToken;
            
            return JsonResultSucesso(RenderRazorViewToString("_GenerateQrCodeFilePartial", model), 
                $"Link e QrCode do arquivo [{request.IdFile}] gerados com sucesso");
            
        }
        catch (Exception error)
        {
            logger.LogError(error, "GenerateQrCodeFile(string::idFile, long::tokenExpirationTime, int::maximumAccessesToken)");
            return TratarErroNegocio(error, $"GenerateQrCodeFile(idFile={request.IdFile}, " +
                                            $"tokenExpirationTime={request.TokenExpirationTime}, " +
                                            $"maximumAccessesToken={request.MaximumAccessesToken})");
        }
        
    }
    
    /// <summary>
    /// Exibe a página de upload de arquivos
    /// </summary>
    [HttpGet]
    public IActionResult Upload(string? applicationCode)
    {
        // Aqui você deve buscar as configurações das aplicações do seu serviço/API
        var model = new FileUploadViewModel
        {
            ApplicationCode = applicationCode??string.Empty,
            Applications = apiService.GetApplications().Result
        };
        return View("FileUpload", model);
    }

    [HttpPost]
    public async Task<IActionResult> JsonUploadInit([FromBody] UploadFileInitDto request)
    {
        try
        {
            // Validações
            if (string.IsNullOrWhiteSpace(request.ApplicationCode))
                return JsonResultErro("Sigla da aplicação é obrigatório");
            if (string.IsNullOrWhiteSpace(request.FileName))
                return JsonResultErro("Nome do arquivo é obrigatório");
            if (request.FileSize <= 0)
                return JsonResultErro("Tamanho do arquivo é obrigatório");
            if (string.IsNullOrWhiteSpace(request.ContentType))
                return JsonResultErro("Tipo do arquivo é obrigatório");

            // Chama o serviço que irá comunicar com a API
            var result = await apiService.UploadFileInit(request);
            return JsonResultSucesso(result, string.Empty);

        }
        catch (Exception ex)
        {
            logger.LogError(ex, "Erro ao iniciar upload de arquivo.");
            return TratarErroNegocio(ex, "JsonUploadInit(UploadFileInitDto::request)");
        }
    }

    [HttpPost]
    [RequestSizeLimit(10_485_760)] // 10MB limite por chunk
    public async Task<IActionResult> JsonUploadChunk()
    {
        try
        {
            
            var uploadId = Request.Form["uploadId"].ToString();
            var chunkIndexStr = Request.Form["chunkIndex"].ToString();
            var totalChunksStr = Request.Form["totalChunks"].ToString();
            var chunkFile = Request.Form.Files["chunk"];

            // Validações
            if (string.IsNullOrWhiteSpace(uploadId))
                return JsonResultErro("UploadId é obrigatório");

            if (!int.TryParse(chunkIndexStr, out int chunkIndex))
                return JsonResultErro("ChunkIndex inválido");

            if (!int.TryParse(totalChunksStr, out int totalChunks))
                return JsonResultErro("TotalChunks inválido");

            if (chunkFile == null || chunkFile.Length == 0)
                return JsonResultErro("Chunk não recebido");

            // Lê o conteúdo do chunk
            using var memoryStream = new MemoryStream();
            await chunkFile.CopyToAsync(memoryStream);
            var chunkData = memoryStream.ToArray();

            // Chama o serviço que irá comunicar com a API
            var result = await apiService.UploadFileSendChunk(new UploadFileChunkDto
            {
                UploadId = uploadId,
                ChunkIndex = chunkIndex,
                TotalChunks = totalChunks,
                ChunkData = chunkData
            });

            return JsonResultSucesso(result, string.Empty);

        }
        catch (Exception ex)
        {
            logger.LogError(ex, "Erro ao enviar chunk do upload.");
            return TratarErroNegocio(ex, "JsonUploadChunk()");
        }
    }
    
    [HttpPost]
    public async Task<IActionResult> JsonUploadFinalize([FromBody] UploadFileIdDto request)
    {
        try {
            
            // Validações
            if (string.IsNullOrWhiteSpace(request.UploadId))
                return JsonResultErro("UploadId é obrigatório");
            
            var result = await apiService.UploadFileFinalize(request.UploadId);
            return JsonResultSucesso(result, string.Empty);
            
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "Erro ao finalizar envio.");
            return TratarErroNegocio(ex, "JsonUploadFinalize(string::uploadId)");
        }
        
    }
    
    [HttpPost]
    public async Task<IActionResult> JsonUploadCancel([FromBody] UploadFileIdDto request)
    {
        try
        {
            // Validações
            if (string.IsNullOrWhiteSpace(request.UploadId))
                return JsonResultErro("UploadId é obrigatório");
            
            var result = await apiService.UploadFileCancel(request.UploadId);
            return JsonResultSucesso(result, string.Empty);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "Erro ao cancelar upload de arquivo-.");
            return TratarErroNegocio(ex, "JsonUploadCancel(string::uploadId)");
        }
    }
    
}