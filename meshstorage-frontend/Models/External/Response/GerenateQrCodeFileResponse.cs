using System.Text.Json.Serialization;
using meshstorage_frontend.Helper.Json;

namespace meshstorage_frontend.Models.External.Response;

public class GerenateQrCodeFileResponse
{
    [JsonPropertyName("idFile")]
    public string IdFile { get; set; } = string.Empty;

    [JsonPropertyName("tokenAccessFile")]
    public string TokenAccessFile { get; set; } = string.Empty;

    [JsonPropertyName("linkAccessFile")]
    public string LinkAccessFile { get; set; } = string.Empty;

    [JsonPropertyName("imageQrCodeAccessFile")]
    public string Base64QrCodeAccessFile { get; set; } = string.Empty;

    [JsonPropertyName("dateTimeRegisteredAccessFile")]
    [JsonConverter(typeof(DateTimeJsonConverter))]
    public DateTime DateTimeRegisteredAccessFile { get; set; }
    
}