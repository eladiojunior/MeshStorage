using meshstorage_frontend.Helper;
using meshstorage_frontend.Models.ViewModels;
using meshstorage_frontend.Services;
using Microsoft.AspNetCore.Mvc;

namespace meshstorage_frontend.Controllers;

public class DashboardController(
    IApiService apiService,
    RazorViewToStringRenderer renderer,
    ILogger<HomeController> logger)
    : DefaultController(renderer, logger)
{
    
    // GET: Dashboard/Index ou Dashboard/
    [HttpGet]
    public IActionResult Index()
    {
        try
        {
            var model = new DashboardModel();
            var systemStatus = apiService.GetSystemStatus().Result;
            model.TotalStorage = systemStatus.TotalStorage;
            model.ConnectedClients = systemStatus.ConnectedClients;
            model.TotalFiles = systemStatus.TotalFiles;
            model.Health = systemStatus.Health;
            model.MessageStatus = systemStatus.MessageStatus;
            model.Status = systemStatus.Status;
            return View(model);
        }
        catch (Exception error)
        {
            return View("Error", new ErrorViewModel(null, error.Message));
        }
    }

    // GET: Dashboard/ListAllStorages
    [HttpGet]
    public ActionResult ListAllStorages()
    {
        try
        {
            var storages = apiService.GetStorages().Result;
            return JsonResultSucesso(RenderRazorViewToString("_InfoStorageCardPartial", storages), "Sucesso");
        }
        catch (Exception error)
        {
            return JsonResultErro(TratarErroNegocio(error, $"ListAllStorage()"));
        }
    }
    
    // GET: Dashboard/ListAllApplications
    [HttpGet]
    public ActionResult ListAllApplications()
    {
        try
        {
            var applications = apiService.GetApplications().Result;
            return JsonResultSucesso(RenderRazorViewToString("_InfoApplicationCardPartial", applications), "Sucesso");
        }
        catch (Exception error)
        {
            return JsonResultErro(TratarErroNegocio(error, $"ListAllApplications()"));
        }
    }
    
}