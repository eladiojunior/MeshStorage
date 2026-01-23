namespace meshstorage_frontend.Models.External.Response;

public class FileContentTypeApiResponse
{
    public int Code { get; set; } 
    public string NameEnum { get; set; } = string.Empty;
    public string Extension { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public string ContentType { get; set; } = string.Empty;
}