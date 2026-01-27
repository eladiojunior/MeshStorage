using meshstorage_frontend.Helper;

namespace meshstorage_frontend.Models.ViewModels;

public class ApplicationViewModel
{
    public long Id { get; set; }
    public string Code { get; set; } = string.Empty;
    public string Name { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public string Icon { get; set; } = string.Empty;
    public long MaximumFileSize { get; set; }
    public string MaxFileSizeFormatted => $"{MaximumFileSize} MB";
    public bool CompressedFileContentToZip { get; set; }
    public bool ConvertImageFileToWebp { get; set; }
    public bool ApplyOcrFileContent { get; set; }
    public bool AllowDuplicateFile { get; set; }
    public bool RequiresFileReplication { get; set; }
    public long TotalFiles { get; set; }

    public List<FileContentTypeViewModel> AllowedFileTypes { get; set; } = [];
    public string[] AllowedExtensions => AllowedFileTypes.Select(s => s.Extension).ToArray();

}