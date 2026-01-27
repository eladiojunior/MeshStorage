using System.Text.Json.Serialization;

namespace meshstorage_frontend.Models.External.Response;

public class UploadFileFinalizeApiResponse
{
    [JsonPropertyName("fileId")]
    public string IdFile { get; set; } = string.Empty;
    
    [JsonPropertyName("status")]
    public string Status { get; set; } = string.Empty;
}