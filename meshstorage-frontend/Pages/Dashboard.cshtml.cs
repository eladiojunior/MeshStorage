using meshstorage_frontend.Services;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace meshstorage_frontend.Pages;

public class Dashboard : PageModel
{
    private readonly IApiService _apiService;

    public Dashboard(IApiService apiService)
    {
        _apiService = apiService;
    }
    
    public string TotalStorage { get; set; }
    public int ConnectedClients { get; set; }
    public int TotalFiles { get; set; }
    public string Health { get; set; }
    public string MessageStatus { get; set; }
    public string Status { get; set; }
    
    public void OnGet()
    {
        var systemStatus = _apiService.getSystemStatus().Result;
        TotalStorage = systemStatus.TotalStorage;
        ConnectedClients = systemStatus.ConnectedClients;
        TotalFiles = systemStatus.TotalFiles;
        Health = systemStatus.Health;
        MessageStatus = systemStatus.MessageStatus;
        Status = systemStatus.Status;;
    }
    
}