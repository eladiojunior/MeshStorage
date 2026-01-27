using System.Text.Json.Serialization;

namespace meshstorage_frontend.Models.External.Request;

public class UploadFileInitApiRequest
{
    [JsonPropertyName("applicationCode")]
    public string ApplicationCode { get; set; } = string.Empty;
    
    [JsonPropertyName("fileName")]
    public string FileName { get; set; } = string.Empty;
    
    [JsonPropertyName("contentType")]
    public string ContentType { get; set; } = string.Empty;
    
    [JsonPropertyName("fileSize")]
    public long FileSize { get; set; }
    
}