namespace meshstorage_frontend.Models.ViewModels;
/// <summary>
/// ViewModel com as informações iniciais di upload de arquivo
/// </summary>
public class UploadFileInitViewModel
{
    public string UploadId { get; set; } = string.Empty;
    public int ChunkSize { get; set; }
    public int TotalChunks { get; set; }
}