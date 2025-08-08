using meshstorage_frontend.Helper;
using meshstorage_frontend.Models.ViewModels;
using meshstorage_frontend.Services;
using Microsoft.AspNetCore.Mvc;

namespace meshstorage_frontend.Controllers;

public class ApplicationController : DefaultController
{
    private readonly IApiService _apiService;
    
    public ApplicationController(IApiService apiService, RazorViewToStringRenderer renderer, 
        ILogger<HomeController> logger) : base(renderer, logger)
    {
        _apiService =  apiService;
    }

    // GET Application/Create
    [HttpGet]
    public IActionResult Create()
    {
        var model = new CreateApplicationViewModel();
        model.ListFileContentType = ListContentTypes();
        return View(model);
    }

    // POST Application/Registre
    [HttpPost]
    public IActionResult Registre(CreateApplicationViewModel model)
    {
        if (!ModelState.IsValid)
            return View("Create", model);

        try
        {
            _apiService.registreApplication(model);

        }
        catch (Exception erro)
        {
            ModelState.AddModelError("_form",  erro.Message);
            return View("Create", model);
        }
        
        return RedirectToAction("Index", "Dashboard");
        
    }
    
    /**
     * Recupera a lista de content types para escolha na aplicação.
     * Dexar as mais utilizadas no início para facilitar a seleção.
     */
    private List<FileContentTypeViewModel> ListContentTypes()
    {
        var listTypes = _apiService.getAllContentTypes();
        return listTypes.Result;
    }
}