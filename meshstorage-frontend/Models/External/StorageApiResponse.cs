namespace meshstorage_frontend.Models.External;

public class StorageApiResponse
{
    public long Id { get; set; }
    public string IdClient { get; set; }
    public string ServerName { get; set; }
    public string StorageName { get; set; }
    public long TotalSpace { get; set; }
    public long FreeSpace { get; set; }
    public long TotalFiles { get; set; }
    public string IpServer { get; set; }
    public string OsServer { get; set; }
    public int StatusCode { get; set; }
    public string StatusDescription { get; set; }
    public string DateTimeRegistered { get; set; }
    public string DateTimeRemoved { get; set; }
    
}