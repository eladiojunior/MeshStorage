using System.ComponentModel.DataAnnotations;

namespace meshstorage_frontend.Models.ViewModels;

public class CreateApplicationViewModel
{
    
    [Required(ErrorMessage = "Nome da aplicação é obrigatório.")]
    [Display(Name = "Nome da aplicação")]
    public string ApplicationName { get; set; }

    [Display(Name = "Descrição da aplicação")]
    public string ApplicationDescription { get; set; }

    [Display(Name = "Tipos de arquivos aceitos")]
    public List<string> AllowedFileTypes { get; set; }

    [Display(Name = "Tamanho dos arquivos (em MB=MegaBytes)")]
    [Required(ErrorMessage = "Informe o tamanho máximo dos arquivos.")]
    [Range(1, 20, ErrorMessage = "Tamanho dos arquivos deve estar entre 1 e 20 MB.")]
    public long MaximumFileSizeMB { get; set; }

    [Display(Name = "Realizar compressão dos arquivos?")]
    public bool CompressedFileContentToZip { get; set; } = false;

    [Display(Name = "Converter imagem em WebP?")]
    public bool ConvertImageFileToWebp { get; set; } = true;
    
    [Display(Name = "Aplicar OCR nos arquivos?")]
    public bool ApplyOcrFileContent { get; set; } = false;

    [Display(Name = "Permitir arquivos duplicados por aplicação?")]
    public bool AllowDuplicateFile { get; set; } = true;

    [Display(Name = "Replicar arquivos em outro servidor de armazenamento?")]
    public bool RequiresFileReplication { get; set; } = false;

    public List<FileContentTypeViewModel> ListFileContentType { get; set; }
    
}