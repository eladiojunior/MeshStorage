namespace meshstorage_frontend.Models.External.Response;

public class StorageApiResponse
{
    public long Id { get; set; }
    public string IdClient { get; set; } = string.Empty;
    public string ServerName { get; set; } = string.Empty;
    public string StorageName { get; set; } = string.Empty;
    public long TotalSpace { get; set; }
    public long FreeSpace { get; set; }
    public long TotalFiles { get; set; }
    public string IpServer { get; set; } = string.Empty;
    public string OsServer { get; set; } = string.Empty;
    public int StatusCode { get; set; }
    public string StatusDescription { get; set; } = string.Empty;
    public string DateTimeRegistered { get; set; } = string.Empty;
    public string DateTimeRemoved { get; set; } = string.Empty;
    
}