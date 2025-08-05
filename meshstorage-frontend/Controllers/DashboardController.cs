using meshstorage_frontend.Models.ViewModels;
using meshstorage_frontend.Services;
using Microsoft.AspNetCore.Mvc;

namespace meshstorage_dashboard.Controllers;

public class DashboardController : Controller
{
    private readonly IApiService _apiService;

    public DashboardController(IApiService apiService)
    {
        _apiService = apiService;
    }
    
    // GET
    public IActionResult Index()
    {
        var model = new DashboardModel();
        var systemStatus = _apiService.getSystemStatus().Result;
        model.TotalStorage = systemStatus.TotalStorage;
        model.ConnectedClients = systemStatus.ConnectedClients;
        model.TotalFiles = systemStatus.TotalFiles;
        model.Health = systemStatus.Health;
        model.MessageStatus = systemStatus.MessageStatus;
        model.Status = systemStatus.Status;;
        return View(model);
    }
    
}