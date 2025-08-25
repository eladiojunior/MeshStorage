using System.Text.Json.Serialization;

namespace meshstorage_frontend.Models.External.Request;

public class UpdateApplicationApiRequest
{
    [JsonPropertyName("applicationCode")]
    public string ApplicationCode { get; set; }
    
    [JsonPropertyName("applicationName")]
    public string ApplicationName { get; set; }
    
    [JsonPropertyName("applicationDescription")]
    public string ApplicationDescription { get; set; }
    
    [JsonPropertyName("maximumFileSizeMB")]
    public long MaximumFileSize { get; set; }
    
    [JsonPropertyName("allowedFileTypes")]
    public string[] AllowedFileTypes { get; set; }
    
    [JsonPropertyName("compressedFileContentToZip")]
    public bool CompressedFileContentToZip { get; set; }
    
    [JsonPropertyName("convertImageFileToWebp")]
    public bool ConvertImageFileToWebp { get; set; }
    
    [JsonPropertyName("applyOcrFileContent")]
    public bool ApplyOcrFileContent { get; set; }
    
    [JsonPropertyName("allowDuplicateFile")]
    public bool AllowDuplicateFile { get; set; }
    
    [JsonPropertyName("requiresFileReplication")]
    public bool RequiresFileReplication { get; set; }
    
}