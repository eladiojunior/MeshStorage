namespace meshstorage_frontend.Models.ViewModels;

public class SystemStatusViewModel
{
    public string TotalStorage { get; set; }
    public int ConnectedClients { get; set; }
    public int TotalFiles { get; set; }
    public string Health { get; set; }
    public string Status { get; set; }
    public string MessageStatus { get; set; }
}