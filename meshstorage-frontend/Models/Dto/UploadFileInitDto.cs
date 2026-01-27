namespace meshstorage_frontend.Models.Dto;

public class UploadFileInitDto
{
    public string ApplicationCode { get; set; } = string.Empty;
    public string FileName { get; set; } = string.Empty;
    public long FileSize { get; set; }
    public string ContentType { get; set; } = string.Empty;
    
}