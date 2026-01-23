namespace meshstorage_frontend.Models.ViewModels;

public class FileContentTypeViewModel
{
    public int Code { get; set; } 
    public string NameEnum { get; set; } = string.Empty;
    public string Extension { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public string ContentType { get; set; } = string.Empty;
}