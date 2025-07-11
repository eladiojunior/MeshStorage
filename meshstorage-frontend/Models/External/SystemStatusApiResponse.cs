namespace meshstorage_frontend.Models.External;

public class SystemStatusApiResponse
{
    public string SystemHealth { get; set; }
    public string MessageStatus { get; set; }
    public int TotalSpaceStorages { get; set; }
    public int TotalFreeStorages { get; set; }
    public int TotalClientsConnected { get; set; }
    public int TotalFilesStorages { get; set; }
    public string DateTimeAvailable { get; set; }
}