namespace meshstorage_frontend.Models.Dto;

public class FilterListFileDto
{
    public string ApplicationCode { get; set; } = string.Empty;
    public string FileLogicName { get; set; } = string.Empty;
    public string[] FileContentType { get; set; } = [];
    public bool FilesSentForBackup { get; set; }
    public bool FilesRemoved { get; set; }
    public int Page { get; set; } = 1;
    public int PageSize { get; set; } = 15;
    public DateTime LastUpdated { get; set; } = DateTime.UtcNow;
}