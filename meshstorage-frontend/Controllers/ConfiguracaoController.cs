using meshstorage_frontend.Helper;
using Microsoft.AspNetCore.Mvc;

namespace meshstorage_frontend.Controllers;

public class ConfiguracaoController : DefaultController
{
    public ConfiguracaoController(RazorViewToStringRenderer renderer, ILogger<HomeController> logger) : base(renderer, logger)
    {
    }

    // GET
    [HttpGet]
    public IActionResult Index()
    {
        return View();
    }
    
}