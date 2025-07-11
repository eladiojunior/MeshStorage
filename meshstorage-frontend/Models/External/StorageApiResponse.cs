namespace meshstorage_frontend.Models.External;

public class StorageApiResponse
{
    public int Id { get; set; }
    public string IdClient { get; set; }
    public string ServerName { get; set; }
    public string StorageName { get; set; }
    public int TotalSpace { get; set; }
    public int FreeSpace { get; set; }
    public string IpServer { get; set; }
    public string OsServer { get; set; }
    public bool Available { get; set; }
    public string DateTimeAvailable { get; set; }
}