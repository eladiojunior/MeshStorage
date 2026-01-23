namespace meshstorage_frontend.Models.External.Response;

public class ApplicationApiResponse
{
    public int Id { get; set; }
    public string ApplicationCode { get; set; } = string.Empty;
    public string ApplicationName { get; set; } = string.Empty;
    public string ApplicationDescription { get; set; } = string.Empty;
    public int MaximumFileSize { get; set; }
    public string[] AllowedFileTypes { get; set; } = [];
    public bool CompressedFileContentToZip { get; set; }
    public bool ConvertImageFileToWebp { get; set; }
    public bool ApplyOcrFileContent { get; set; }
    public bool AllowDuplicateFile { get; set; }
    public bool RequiresFileReplication { get; set; }
    public int TotalFiles { get; set; }
    public string DateTimeApplication { get; set; } = string.Empty;
}