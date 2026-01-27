namespace meshstorage_frontend.Models.ViewModels;
/// <summary>
/// ViewModel com resultado de confirmação do processo de upload de arquivo
/// </summary>
public class UploadFileFinalizeViewModel
{
    public string IdFile { get; set; } = string.Empty;
    public string Status { get; set; } = string.Empty;
}