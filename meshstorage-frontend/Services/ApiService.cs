using System.Net;
using System.Text.Json;
using meshstorage_frontend.Helper;
using meshstorage_frontend.Models.External;
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
    
    private async Task<string> RequestGet(string endpoint, string apiKey = "") {
        
        using var request = new HttpRequestMessage(HttpMethod.Get, endpoint);
        request.Headers.Accept.Add(new System.Net.Http.Headers.MediaTypeWithQualityHeaderValue("application/json"));
        if (!string.IsNullOrEmpty(apiKey))
            request.Headers.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", apiKey);

        using var response = await _httpClient.SendAsync(request);
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

    private async Task<string> RequestPost<T>(string endpoint, T payload, string? apiKey = null)
    {
        using var request = new HttpRequestMessage(HttpMethod.Post, endpoint);
        request.Headers.Accept.Add(new System.Net.Http.Headers.MediaTypeWithQualityHeaderValue("application/json"));

        if (!string.IsNullOrEmpty(apiKey))
            request.Headers.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", apiKey);

        // Serializa o objeto para JSON e adiciona no body
        var json = System.Text.Json.JsonSerializer.Serialize(payload);
        request.Content = new StringContent(json, System.Text.Encoding.UTF8, "application/json");

        using var response = await _httpClient.SendAsync(request);

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
    
    public Task<SystemStatusViewModel> getSystemStatus()
    {
        SystemStatusApiResponse? response = null;
        var json = RequestGet("/api/v1/system/status").Result;
        response = JsonSerializer.Deserialize<SystemStatusApiResponse>(json, _jsonSerializerOptions);
        return Task.FromResult(_mapper.MapperSystemStatus(response));
    }

    public Task<List<StorageViewModel>> getStorages()
    {
        var json = RequestGet("/api/v1/storage/list?available=false").Result;
        var response = JsonSerializer.Deserialize<StorageApiResponse[]>(json, _jsonSerializerOptions);
        return Task.FromResult(_mapper.MapperStorage(response));
    }

    public Task<List<ApplicationViewModel>> getApplications()
    {
        var json = RequestGet("/api/v1/application/list").Result;
        var response = JsonSerializer.Deserialize<ApplicationApiResponse[]>(json, _jsonSerializerOptions);
        return Task.FromResult(_mapper.MapperApplication(response, getAllContentTypes().Result));
    }

    public Task<List<FileContentTypeViewModel>> getAllContentTypes()
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

    public Task<ApplicationViewModel> registreApplication(CreateApplicationViewModel model)
    {
        var request = _mapper.MapperApplication(model);
        var json = RequestPost("/api/v1/application/register", request).Result;
        var response = JsonSerializer.Deserialize<ApplicationApiResponse>(json, _jsonSerializerOptions);
        return Task.FromResult(_mapper.MapperApplication(response, getAllContentTypes().Result));

    }

}