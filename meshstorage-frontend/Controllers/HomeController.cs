using System.Diagnostics;
using Microsoft.AspNetCore.Mvc;
using meshstorage_frontend.Models.ViewModels;
using meshstorage_frontend.Helper;

namespace meshstorage_frontend.Controllers;

public class HomeController(
    RazorViewToStringRenderer renderer) :
    DefaultController(renderer)
{
    
    public IActionResult Index()
    {
        return View();
    }

    [ResponseCache(Duration = 0, Location = ResponseCacheLocation.None, NoStore = true)]
    public IActionResult Error()
    {
        return View(new ErrorViewModel(Activity.Current?.Id ?? HttpContext.TraceIdentifier, "Falha no servidor."));
    }
    
}