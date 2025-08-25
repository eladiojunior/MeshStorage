using System.ComponentModel.DataAnnotations;
using meshstorage_frontend.Helper;

namespace meshstorage_frontend.Models.ViewModels;

public class EditApplicationViewModel
{
    public long IdApplication { get; set; }
    
    [Display(Name = "Sigla da aplicação")]
    [Required(ErrorMessage = "Sigla obrigatória.")]
    [MaxLength(10, ErrorMessage = "Informe no máximo 10 caracteres sem espaço.")]
    [RegularExpression(@"^\S+$", ErrorMessage = "Sigla inválida, informe no máximo 10 caracteres sem espaço.")]
    public string ApplicationCode { get; set; }
    
    [Display(Name = "Nome da aplicação")]
    [Required(ErrorMessage = "Nome da aplicação obrigatório.")]
    [MaxLength(50, ErrorMessage = "Informe no máximo 50 caracteres.")]
    public string ApplicationName { get; set; }

    [Display(Name = "Descrição da aplicação")]
    [Required(ErrorMessage = "Descrição da aplicação obrigatória.")]
    [MaxLength(500, ErrorMessage = "Informe no máximo 500 caracteres.")]
    public string ApplicationDescription { get; set; }

    [Display(Name = "Tipos de arquivos aceitos para upload")]
    [Required(ErrorMessage = "Informe pelo menos um tipo de arquivo (Content Type) permitido.")]
    public string AllowedFileTypes { get; set; }

    [Display(Name = "Tamanho dos arquivos (em MB=MegaBytes)")]
    [Required(ErrorMessage = "Informe o tamanho máximo dos arquivos.")]
    [Range(1, 20, ErrorMessage = "Tamanho dos arquivos deve estar entre 1 e 20 MB.")]
    public long MaximumFileSizeMB { get; set; } = 1;

    [Display(Name = "Realizar compressão dos arquivos para reduzir o tamanho?")]
    public bool CompressedFileContentToZip { get; set; } = false;

    [Display(Name = "Converter imagem em WebP para reduzir o tamanho?")]
    public bool ConvertImageFileToWebp { get; set; } = true;
    
    [Display(Name = "Aplicar OCR nos arquivos PDF (com imagem) e Imagens?")]
    public bool ApplyOcrFileContent { get; set; } = false;

    [Display(Name = "Não permitir arquivos duplicados por aplicação?")]
    public bool AllowDuplicateFile { get; set; } = true;

    [Display(Name = "Replicar arquivos em outro servidor de armazenamento?")]
    public bool RequiresFileReplication { get; set; } = false;

}