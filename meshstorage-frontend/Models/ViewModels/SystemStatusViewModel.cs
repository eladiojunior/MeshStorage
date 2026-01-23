namespace meshstorage_frontend.Models.ViewModels;

public class SystemStatusViewModel
{
    public string TotalStorage { get; set; } = string.Empty;
    public int ConnectedClients { get; set; }
    public int TotalFiles { get; set; }
    public string Health { get; set; } = string.Empty;
    public string Status { get; set; } = string.Empty;
    public string MessageStatus { get; set; } = string.Empty;
}