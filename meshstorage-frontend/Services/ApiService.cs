using System.Net;
using System.Net.Http.Headers;
using System.Text.Json;
using meshstorage_frontend.Helper;
using meshstorage_frontend.Models.Dto;
using meshstorage_frontend.Models.External.Response;
using meshstorage_frontend.Models.ViewModels;
using meshstorage_frontend.Services.Cache;
using meshstorage_frontend.Services.Exceptions;
using meshstorage_frontend.Settings;
using Microsoft.Extensions.Options;

namespace meshstorage_frontend.Services;

public class ApiService : IApiService
{
    private readonly ICacheHelper _cache;
    private readonly HttpClient _httpClient;
    private readonly MapperHelper _mapper;
    
    private readonly JsonSerializerOptions? _jsonSerializerOptions = new()
    {
        PropertyNameCaseInsensitive = true
    };
    
    public ApiService(HttpClient httpClient, IOptions<ApiSettings> options, ICacheHelper cache, MapperHelper mapper)
    {
        _cache = cache;
        _mapper = mapper;
        _httpClient = httpClient;
        var settings = options.Value;
        _httpClient.BaseAddress = new Uri(settings.BaseUrl);
    }
    
    private async Task<string> ReadResponse(HttpResponseMessage response)
    {
        
        if (response.IsSuccessStatusCode)
            return await response.Content.ReadAsStringAsync();
        
        //Tratar erro na retorno da API.
        var error = await response.Content.ReadAsStringAsync();
        if (response.StatusCode is HttpStatusCode.BadRequest or HttpStatusCode.InternalServerError)
        {
            var responseErro = JsonSerializer.Deserialize<ErroApiResponse>(error, _jsonSerializerOptions);
            if (responseErro != null)
                throw new ApiBusinessException(responseErro.Code, responseErro.Menssage);
        }
        //Lançar erro genérico...
        throw new Exception($"API error: {(int)response.StatusCode} - {error}");
        
    }
    
    private async Task<string> RequestGet(string endpoint, string apiKey = "") {
        
        using var request = new HttpRequestMessage(HttpMethod.Get, endpoint);
        request.Headers.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
        if (!string.IsNullOrEmpty(apiKey))
            request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", apiKey);
        
        using var response = await _httpClient.SendAsync(request);
        return await ReadResponse(response);
        
    }

    private async Task<string> RequestPost<T>(string endpoint, T payload, string? apiKey = null)
    {
        using var request = new HttpRequestMessage(HttpMethod.Post, endpoint);
        request.Headers.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));

        if (!string.IsNullOrEmpty(apiKey))
            request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", apiKey);

        // Serializa o objeto para JSON e adiciona no body
        var json = JsonSerializer.Serialize(payload);
        request.Content = new StringContent(json, System.Text.Encoding.UTF8, "application/json");

        using var response = await _httpClient.SendAsync(request);
        return await ReadResponse(response);
        
    }
    private async Task<string> RequestPost(string endpoint, string? apiKey = null)
    {
        using var request = new HttpRequestMessage(HttpMethod.Post, endpoint);
        request.Headers.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));

        if (!string.IsNullOrEmpty(apiKey))
            request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", apiKey);

        using var response = await _httpClient.SendAsync(request);
        return await ReadResponse(response);
        
    }
    
    private async Task<string> RequestPut<T>(string endpoint, T payload, string? apiKey = null)
    {
        using var request = new HttpRequestMessage(HttpMethod.Put, endpoint);
        request.Headers.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));

        if (!string.IsNullOrEmpty(apiKey))
            request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", apiKey);

        // Serializa o objeto para JSON e adiciona no body
        var json = JsonSerializer.Serialize(payload);
        request.Content = new StringContent(json, System.Text.Encoding.UTF8, "application/json");

        using var response = await _httpClient.SendAsync(request);
        return await ReadResponse(response);

    }

    private async Task<string> RequestDelete<T>(string endpoint, T payload, string? apiKey = null)
    {
            
        using var request = new HttpRequestMessage(HttpMethod.Delete, endpoint);
        request.Headers.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));

        if (!string.IsNullOrEmpty(apiKey))
            request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", apiKey);

        // Serializa o objeto para JSON e adiciona no body
        if (payload != null)
        {
            var json = JsonSerializer.Serialize(payload);
            request.Content = new StringContent(json, System.Text.Encoding.UTF8, "application/json");
        }

        using var response = await _httpClient.SendAsync(request);
        return await ReadResponse(response);
            
    }

    public Task<SystemStatusViewModel> GetSystemStatus()
    {
        var json = RequestGet("/api/v1/system/status").Result;
        var response = JsonSerializer.Deserialize<SystemStatusApiResponse>(json, _jsonSerializerOptions);
        return Task.FromResult(_mapper.MapperSystemStatus(response));
    }

    public Task<List<StorageViewModel>> GetStorages()
    {
        var json = RequestGet("/api/v1/storage/list?available=false").Result;
        var response = JsonSerializer.Deserialize<StorageApiResponse[]>(json, _jsonSerializerOptions);
        return Task.FromResult(_mapper.MapperStorage(response));
    }

    public Task<List<ApplicationViewModel>> GetApplications()
    {
        var json = RequestGet("/api/v1/application/list").Result;
        var response = JsonSerializer.Deserialize<ApplicationApiResponse[]>(json, _jsonSerializerOptions);
        return Task.FromResult(_mapper.MapperApplication(response, GetAllContentTypes().Result));
    }

    public Task<ApplicationViewModel?> GetApplication(long idApplication)
    {
        var json = RequestGet("/api/v1/application/getById/"+idApplication).Result;
        var response = JsonSerializer.Deserialize<ApplicationApiResponse>(json, _jsonSerializerOptions);
        return Task.FromResult(_mapper.MapperApplication(response, GetAllContentTypes().Result));
    }

    public Task<List<FileContentTypeViewModel>> GetAllContentTypes()
    {
        var result = _cache.ListCache(
            CacheHelper.CacheContentTypeKey, () =>
            {
                var json = RequestGet("/api/v1/file/listContentTypes").Result;
                var response = JsonSerializer.Deserialize<FileContentTypeApiResponse[]>(json, _jsonSerializerOptions);
                return _mapper.MapperFileContentType(response);
            }
        );
        return Task.FromResult(result.ToList());
    }

    public Task<ApplicationViewModel?> RegistreApplication(CreateApplicationViewModel model)
    {
        var request = _mapper.MapperApplication(model);
        var json = RequestPost("/api/v1/application/register", request).Result;
        var response = JsonSerializer.Deserialize<ApplicationApiResponse>(json, _jsonSerializerOptions);
        return Task.FromResult(_mapper.MapperApplication(response, GetAllContentTypes().Result));

    }

    public Task<ApplicationViewModel?> EditApplication(EditApplicationViewModel model)
    {
        var request = _mapper.MapperApplication(model);
        var json = RequestPut("/api/v1/application/update/"+model.IdApplication, request).Result;
        var response = JsonSerializer.Deserialize<ApplicationApiResponse>(json, _jsonSerializerOptions);
        return Task.FromResult(_mapper.MapperApplication(response, GetAllContentTypes().Result));
    }

    public Task<PagedResultViewModel<FileItemViewModel, FilterListFileViewModel>> ListFilesFilter
        (FilterListFileDto filter)
    {
        
        if (string.IsNullOrEmpty(filter.ApplicationCode))
            throw new ApiBusinessException(400, "Sigla da aplicação não informada.");
            
        var url = "/api/v1/file/listPaginated?applicationCode=" + filter.ApplicationCode;
        if (!string.IsNullOrEmpty(filter.FileLogicName))
            url += "&fileLogicName=" + filter.FileLogicName;
        if (filter.FileContentType.Length != 0)
            url = filter.FileContentType.Aggregate(url, (current, contentType) => 
                current + ("&fileContentType=" + contentType));
        url += "&pageNumber=" + filter.Page +
               "&recordsPerPage=" + filter.PageSize +
               "&isFilesSentForBackup=" + filter.FilesSentForBackup +
               "&isFilesRemoved=" + filter.FilesRemoved;
        var json = RequestGet(url).Result;
        var response = JsonSerializer.Deserialize<ListFilesApiResponse>(json, _jsonSerializerOptions);
        var model = _mapper.MapperListFile(response, GetAllContentTypes().Result);
        model.Filter = _mapper.MapperFilterListFile(filter);
        model.Page = filter.Page;
        model.PageSize = filter.PageSize;
        return Task.FromResult(model);
    }

    public void RemoveStorage(long idServerStorage)
    {
        if (idServerStorage == 0)
            throw new ApiBusinessException(400, "Identificador do Server Storage não informado.");

        _ = RequestDelete<string>($"/api/v1/storage/remove/{idServerStorage}", null!).Result;
        
    }

    public async Task<(Stream Stream, string ContentType, string FileName)> DownloadFile(string idFile, 
        string? userName, string? accessChannel)
    {
        
        if (string.IsNullOrEmpty(idFile))
            throw new ApiBusinessException(400, "Identificador do arquivo não informado.");

        var request = new HttpRequestMessage(
            HttpMethod.Get,
            $"/api/v1/file/download/{idFile}"
        );

        if (!string.IsNullOrEmpty(userName))
            request.Headers.Add("X-User-Name", userName);
        if (!string.IsNullOrEmpty(accessChannel))
            request.Headers.Add("X-Access-Channel", "Site");

        var response = await _httpClient.SendAsync(
            request,
            HttpCompletionOption.ResponseHeadersRead
        );

        if (response.IsSuccessStatusCode)
        {
            var stream = await response.Content.ReadAsStreamAsync();
            var contentType =
                response.Content.Headers.ContentType?.MediaType ?? "application/octet-stream";
            var fileName =
                response.Content.Headers.ContentDisposition?.FileNameStar ??
                response.Content.Headers.ContentDisposition?.FileName ??
                $"arquivo-{idFile}";
            //Retirar as aspas duplas se vier com...
            contentType = contentType.Replace("\"", "");
            fileName = fileName.Replace("\"", "");
            return (stream, contentType, fileName);
        }
    
        //Tratar erro na retorno da API.
        var error = await response.Content.ReadAsStringAsync();
        if (response.StatusCode is HttpStatusCode.BadRequest or HttpStatusCode.InternalServerError)
        {
            var responseErro = JsonSerializer.Deserialize<ErroApiResponse>(error, _jsonSerializerOptions);
            if (responseErro != null)
                throw new ApiBusinessException(responseErro.Code, responseErro.Menssage);
        }
        
        //Lançar erro genérico...
        throw new Exception($"API error: {(int)response.StatusCode} - {error}");
        
    }

    public void RemoveFile(string idFile)
    {
        if (string.IsNullOrEmpty(idFile))
            throw new ApiBusinessException(400, "Identificador do arquivo não informado.");
        
        _ = RequestDelete<string>($"/api/v1/file/remove/{idFile}", null!).Result;
        
    }

    public Task<FileQrCodeViewModel?> GenerateLinkQrCodeFile(string idFile, long tokenExpirationTime, int maximumAccessesToken)
    {
        if (string.IsNullOrEmpty(idFile))
            throw new ApiBusinessException(400, "Identificador do arquivo não informado.");
        
        var url = $"/api/v1/file/qrcode/{idFile}?tokenExpirationTime={tokenExpirationTime}" +
                  $"&maximumAccessestoken={maximumAccessesToken}";
        var json = RequestGet(url).Result;
        var response = JsonSerializer.Deserialize<GerenateQrCodeFileResponse>(json, _jsonSerializerOptions);
        return Task.FromResult(_mapper.MapperGenerateQrCodeFile(response));
    }

    public Task<UploadFileInitViewModel> UploadFileInit(UploadFileInitDto uploadFileInit)
    {
        var request = _mapper.MapperUploadFileInit(uploadFileInit);
        var json = RequestPost("/api/v1/file/uploadInChunk/init", request).Result;
        var response = JsonSerializer.Deserialize<UploadFileInitApiResponse>(json, _jsonSerializerOptions);
        var model = _mapper.MapperUploadFileInit(response);
        if (model == null)
            throw new ApiBusinessException(500, "Erro na inicialização do upload do arquivo.");
        return Task.FromResult(model);
    }

    public async Task<UploadFileOkViewModel> UploadFileSendChunk(UploadFileChunkDto dto)
    {

        using var formContent = new MultipartFormDataContent();
            
        var chunkContent = new ByteArrayContent(dto.ChunkData);
        chunkContent.Headers.ContentType = new MediaTypeHeaderValue("application/octet-stream");
        formContent.Add(chunkContent, "chunkBlob", "chunk.bin");

        var response = await _httpClient.PutAsync(
            $"/api/v1/file/uploadInChunk/chunk?uploadId={dto.UploadId}" +
            $"&chunkIndex={dto.ChunkIndex}&chunkTotal={dto.TotalChunks}",
            formContent
        );

        if (response.IsSuccessStatusCode)
        {
            return new UploadFileOkViewModel
            {
                UploadId = dto.UploadId
            };
        }

        //Tratar erro na retorno da API.
        var error = await response.Content.ReadAsStringAsync();
        if (response.StatusCode is HttpStatusCode.BadRequest or HttpStatusCode.InternalServerError)
        {
            var responseErro = JsonSerializer.Deserialize<ErroApiResponse>(error, _jsonSerializerOptions);
            if (responseErro != null)
                throw new ApiBusinessException(responseErro.Code, responseErro.Menssage);
        }
    
        //Lançar erro genérico...
        throw new Exception($"API error: {(int)response.StatusCode} - {error}");
            
    }

    public Task<UploadFileFinalizeViewModel> UploadFileFinalize(string uploadId)
    {
        var json = RequestPost($"/api/v1/file/uploadInChunk/finalize/{uploadId}").Result;
        var response = JsonSerializer.Deserialize<UploadFileFinalizeApiResponse>(json, _jsonSerializerOptions);
        var model = _mapper.MapperUploadFileFinalize(response);
        if (model == null)
            throw new ApiBusinessException(500, "Erro na finalização do upload do arquivo.");
        return Task.FromResult(model);
    }

    public Task<UploadFileOkViewModel> UploadFileCancel(string uploadId)
    {
        _ = RequestPost($"/api/v1/file/uploadInChunk/cancel/{uploadId}").Result;
        var model = new UploadFileOkViewModel
        {
            UploadId = uploadId
        };
        return Task.FromResult(model);
    }
    
}