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
    
    public string IconFile =>
        FileExtension switch
        {
            ".pdf" => "picture_as_pdf",
            ".zip" => "folder_zip",
            ".jpg" or ".jpeg" or ".png" or ".webp" => "image",
            ".doc" or ".docx" => "text_snippet",
            _ => "insert_drive_file"
        };
    public string FileLengthFormatted =>
        FormatterHelper.FileSizeFormat(FileLength);
}