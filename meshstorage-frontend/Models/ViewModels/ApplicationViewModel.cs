namespace meshstorage_frontend.Models.ViewModels;

public class ApplicationViewModel
{
    public long Id { get; set; }
    public string Code { get; set; }
    public string Name { get; set; }
    public string Description { get; set; }
    public string Icon { get; set; }
    public long MaximumFileSize { get; set; }
    public bool CompressedFileContentToZip { get; set; }
    public bool ConvertImageFileToWebp { get; set; }
    public bool ApplyOcrFileContent { get; set; }
    public bool AllowDuplicateFile { get; set; }
    public bool RequiresFileReplication { get; set; }
    public long TotalFiles { get; set; }
    
    public List<FileContentTypeViewModel> AllowedFileTypes { get; set; }
    
}