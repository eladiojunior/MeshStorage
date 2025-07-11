namespace meshstorage_frontend.Models.External;

public class ApplicationApiResponse
{
    public int Id { get; set; }
    public string ApplicationName { get; set; }
    public string ApplicationDescription { get; set; }
    public int MaximumFileSize { get; set; }
    public string[] AllowedFileTypes { get; set; }
    public bool CompressFileContent { get; set; }
    public bool ApplyOcrFileContent { get; set; }
    public bool AllowDuplicateFile { get; set; }
    public int TotalFiles { get; set; }
    public string DateTimeApplication { get; set; }
}