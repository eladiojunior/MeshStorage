using meshstorage_frontend.Helper;

namespace meshstorage_frontend.Models.ViewModels;

public class FileItemViewModel
{
    public string IdFile { get; set; } = string.Empty;
    public string FileLogicName { get; set; } = string.Empty;
    public string FileFisicalName { get; set; } = string.Empty;
    public string FileContentType { get; set; } = string.Empty;
    public string FileExtension { get; set; } = string.Empty;
    public long FileLength { get; set; }
    public string HashFileBytes { get; set; } = string.Empty;
    public bool CompressedFileContent { get; set; }
    public double PercentualCompressedFile { get; set; }
    public DateTime DtRegisteredFileStorage { get; set; }
    public string FileStatusDescription { get; set; } = string.Empty;
    
    public string IconClass =>
        FileExtension switch
        {
            "pdf" => "bi-file-earmark-pdf text-danger",
            "jpg" or "jpeg" or "png" or "webp" => "bi-file-earmark-image text-success",
            "doc" or "docx" => "bi-file-earmark-word text-primary",
            _ => "bi-file-earmark"
        };
    public string FileLengthFormatted =>
        FileSizeFormatter.Format(FileLength);
}