using System.Text.Json.Serialization;

namespace meshstorage_frontend.Models.External.Response;

public class ListFilesApiResponse
{
    [JsonPropertyName("totalRecords")]
    public int TotalRecords { get; set; }

    [JsonPropertyName("files")]
    public List<FileItemResponse> Files { get; set; } = new();
}