using meshstorage_frontend.Helper;
using meshstorage_frontend.Models.ViewModels;
using meshstorage_frontend.Services;
using Microsoft.AspNetCore.Mvc;

namespace meshstorage_frontend.Controllers;

public class ApplicationController : DefaultController
{
    private readonly IApiService _apiService;
    
    public ApplicationController(IApiService apiService, RazorViewToStringRenderer renderer, ILogger<HomeController> logger) : base(renderer, logger)
    {
        _apiService =  apiService;
    }

    // GET Application/New
    [HttpGet]
    public IActionResult New()
    {
        var model = new NewApplicationViewModel();
        return View(model);
    }
}