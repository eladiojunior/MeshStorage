namespace meshstorage_frontend.Models.External;

public class ApplicationApiResponse
{
    public int Id { get; set; }
    public string ApplicationCode { get; set; }
    public string ApplicationName { get; set; }
    public string ApplicationDescription { get; set; }
    public int MaximumFileSize { get; set; }
    public string[] AllowedFileTypes { get; set; }
    public bool CompressedFileContentToZip { get; set; }
    public bool ConvertImageFileToWebp { get; set; }
    public bool ApplyOcrFileContent { get; set; }
    public bool AllowDuplicateFile { get; set; }
    public bool RequiresFileReplication { get; set; }
    public int TotalFiles { get; set; }
    public string DateTimeApplication { get; set; }
}