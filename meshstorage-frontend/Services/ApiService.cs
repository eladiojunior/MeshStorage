using System.Text.Json;
using meshstorage_frontend.Helper;
using meshstorage_frontend.Models.External;
using meshstorage_frontend.Models.ViewModels;
using meshstorage_frontend.Settings;
using Microsoft.Extensions.Options;

namespace meshstorage_frontend.Services;

public class ApiService : IApiService
{
    private readonly HttpClient _httpClient;
    private readonly ApiSettings _settings;
    private readonly JsonSerializerOptions _jsonSerializerOptions = new JsonSerializerOptions
    {
        PropertyNameCaseInsensitive = true
    };
    
    public ApiService(HttpClient httpClient, IOptions<ApiSettings> options)
    {
        _httpClient = httpClient;
        _settings = options.Value;
        _httpClient.BaseAddress = new Uri(_settings.BaseUrl);
    }
    
    private async Task<string> Request(string endpoint, string apiKey = "") {
        
        using var request = new HttpRequestMessage(HttpMethod.Get, endpoint);
        request.Headers.Accept.Add(new System.Net.Http.Headers.MediaTypeWithQualityHeaderValue("application/json"));
        if (!string.IsNullOrEmpty(apiKey))
            request.Headers.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", apiKey);

        using var response = await _httpClient.SendAsync(request);
        if (!response.IsSuccessStatusCode)
        {
            var error = await response.Content.ReadAsStringAsync();
            throw new Exception($"API error: {(int)response.StatusCode} - {response.ReasonPhrase} - {error}");
        }

        return await response.Content.ReadAsStringAsync();
        
    }
    
    public Task<SystemStatusViewModel> getSystemStatus()
    {
        SystemStatusApiResponse response = null;
        try
        {
            var json = Request("/api/v1/system/status").Result;
            response = JsonSerializer.Deserialize<SystemStatusApiResponse>(json, _jsonSerializerOptions);
        }
        catch (Exception erro)
        {
            Console.WriteLine(erro.Message);
        }
        return Task.FromResult(MapperHelper.MapperSystemStatus(response));
    }

    public Task<List<StorageViewModel>> getStorages()
    {
        var json = Request("/api/v1/storage/list?available=false").Result;
        var response = JsonSerializer.Deserialize<StorageApiResponse[]>(json, _jsonSerializerOptions);
        return Task.FromResult(MapperHelper.MapperStorage(response));
    }

    public Task<List<ApplicationViewModel>> getApplications()
    {
        var json = Request("/api/v1/application/list").Result;
        var response = JsonSerializer.Deserialize<ApplicationApiResponse[]>(json, _jsonSerializerOptions);
        return Task.FromResult(MapperHelper.MapperApplication(response));
    }

    public Task<List<FileContentTypeViewModel>> getAllContentTypes()
    {
        var json = Request("/api/v1/file/listContentTypes").Result;
        var response = JsonSerializer.Deserialize<FileContentTypeApiResponse[]>(json, _jsonSerializerOptions);
        return Task.FromResult(MapperHelper.MapperFileContentType(response));
    }

    public void registreApplication(CreateApplicationViewModel model)
    {
        throw new NotImplementedException();
    }
}