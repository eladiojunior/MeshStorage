using System.Text.Json.Serialization;

namespace meshstorage_frontend.Models.External.Response;

public class FileCompressedResponse
{
    [JsonPropertyName("compressedFileLength")]
    public long CompressedFileLength { get; set; }

    [JsonPropertyName("compressedFileContentType")]
    public string CompressedFileContentType { get; set; } = string.Empty;

    [JsonPropertyName("compressedFileInformation")]
    public string CompressedFileInformation { get; set; } = string.Empty;

    [JsonPropertyName("compressedHashFileBytes")]
    public string CompressedHashFileBytes { get; set; } = string.Empty;

    [JsonPropertyName("percentualCompressedFile")]
    public double PercentualCompressedFile { get; set; }
}