
namespace meshstorage_frontend.Models.ViewModels;

public class FilterListFileViewModel
{
    public string ApplicationCode { get; set; } = string.Empty;
    public string? FileLogicName { get; set; } = string.Empty;
    public string? FileContentType { get; set; } = string.Empty;
    public bool FilesSentForBackup { get; set; }
    public bool FilesRemoved { get; set; }
}