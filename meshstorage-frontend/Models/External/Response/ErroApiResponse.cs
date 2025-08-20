using System.Text.Json.Serialization;

namespace meshstorage_frontend.Models.External.Response;

public class ErroApiResponse
{
    [JsonPropertyName("codeError")]
    public int Code { get; set; }
    
    [JsonPropertyName("messageError")]
    public string Menssage { get; set; }
}