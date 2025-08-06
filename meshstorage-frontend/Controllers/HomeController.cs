using System.Diagnostics;
using Microsoft.AspNetCore.Mvc;
using meshstorage_frontend.Models;
using meshstorage_frontend.Helper;

namespace meshstorage_frontend.Controllers;

public class HomeController : DefaultController
{
    public HomeController(RazorViewToStringRenderer renderer, ILogger<HomeController> logger) : 
        base(renderer, logger) { }

    public IActionResult Index()
    {
        return View();
    }

    [ResponseCache(Duration = 0, Location = ResponseCacheLocation.None, NoStore = true)]
    public IActionResult Error()
    {
        return View(new ErrorViewModel { RequestId = Activity.Current?.Id ?? HttpContext.TraceIdentifier });
    }
    
}