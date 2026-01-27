namespace meshstorage_frontend.Models.ViewModels;
/// <summary>
/// ViewModel para a página de upload de arquivos
/// </summary>
public class FileUploadViewModel
{
    /// <summary>
    /// Lista de aplicações disponíveis para upload
    /// </summary>
    public List<ApplicationViewModel> Applications { get; set; } = new();
    public string ApplicationCode { get; set; } = string.Empty;
}