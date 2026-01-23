using System.Text.Json.Serialization;
using meshstorage_frontend.Helper.Json;

namespace meshstorage_frontend.Models.External.Response;

public class FileItemResponse
{
    [JsonPropertyName("idFile")]
    public string IdFile { get; set; } = string.Empty;

    [JsonPropertyName("fileLogicName")]
    public string FileLogicName { get; set; } = string.Empty;

    [JsonPropertyName("fileFisicalName")]
    public string FileFisicalName { get; set; } = string.Empty;

    [JsonPropertyName("fileContentType")]
    public string FileContentType { get; set; } = string.Empty;

    [JsonPropertyName("fileLength")]
    public long FileLength { get; set; }

    [JsonPropertyName("hashFileBytes")]
    public string HashFileBytes { get; set; } = string.Empty;

    [JsonPropertyName("extractionTextFileByOcr")]
    public bool ExtractionTextFileByOcr { get; set; }

    [JsonPropertyName("compressedFileContent")]
    public bool CompressedFileContent { get; set; }

    [JsonPropertyName("dateTimeRegisteredFileStorage")]
    [JsonConverter(typeof(DateTimeJsonConverter))]
    public DateTime DateTimeRegisteredFileStorage { get; set; }

    [JsonPropertyName("dateTimeRemovedFileStorage")]
    [JsonConverter(typeof(NullableDateTimeJsonConverter))]
    public DateTime? DateTimeRemovedFileStorage { get; set; }

    [JsonPropertyName("dateTimeBackupFileStorage")]
    [JsonConverter(typeof(NullableDateTimeJsonConverter))]
    public DateTime? DateTimeBackupFileStorage { get; set; }

    [JsonPropertyName("fileStatusCode")]
    public int FileStatusCode { get; set; }

    [JsonPropertyName("fileStatusDescription")]
    public string FileStatusDescription { get; set; } = string.Empty;

    [JsonPropertyName("fileExtractionByOcr")]
    public bool? FileExtractionByOcr { get; set; }

    [JsonPropertyName("fileCompressed")]
    public FileCompressedResponse? FileCompressed { get; set; }
}