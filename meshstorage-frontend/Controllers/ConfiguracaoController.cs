using meshstorage_frontend.Helper;
using Microsoft.AspNetCore.Mvc;

namespace meshstorage_frontend.Controllers;

public class ConfiguracaoController(
    RazorViewToStringRenderer renderer) : 
    DefaultController(renderer)
{
    // GET
    [HttpGet]
    public IActionResult Index()
    {
        return View();
    }
    
}