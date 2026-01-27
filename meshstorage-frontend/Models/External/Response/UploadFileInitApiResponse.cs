using System.Text.Json.Serialization;

namespace meshstorage_frontend.Models.External.Response;

public class UploadFileInitApiResponse
{
    [JsonPropertyName("uploadId")]
    public string UploadId { get; set; } = string.Empty;
    [JsonPropertyName("chunkSize")]
    public int ChunkSize { get; set; }
    [JsonPropertyName("chunkTotal")]
    public int ChunkTotal { get; set; }
}