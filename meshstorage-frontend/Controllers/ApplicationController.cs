using meshstorage_frontend.Helper;
using meshstorage_frontend.Models.ViewModels;
using meshstorage_frontend.Services;
using Microsoft.AspNetCore.Mvc;

namespace meshstorage_frontend.Controllers;

public class ApplicationController(
    IApiService apiService,
    RazorViewToStringRenderer renderer,
    ILogger<HomeController> logger)
    : DefaultController(renderer, logger)
{
    
    // GET Application/Create
   [HttpGet]
   public IActionResult Create()
    {
        var model = new CreateApplicationViewModel();
        return View(model);
    }

    // GET Application/ListFileContentTypes
    [HttpGet]
    public IActionResult ListFileContentTypes()
    {
        var listFileContentTypes = ListContentTypes();
        return JsonResultSucesso(listFileContentTypes, "Sucesso");
    }
        
    // POST Application/Registre
    [HttpPost]
    public IActionResult Registre(CreateApplicationViewModel model)
    {
        if (!ModelState.IsValid)
            return View("Create", model);

        try
        {
            
            apiService.RegistreApplication(model);

        }
        catch (Exception erro)
        {
            ModelState.AddModelError("_form",  erro.Message);
            return View("Create", model);
        }

        return RedirectToActionByMessage("Index", "Dashboard", 
            false, "Aplicação registrada com sucesso.");
        
    }
    
    /**
     * Recupera a lista de content types para escolha na aplicação.
     * Dexar as mais utilizadas no início para facilitar a seleção.
     */
    private List<FileContentTypeViewModel> ListContentTypes()
    {
        var listTypes = apiService.GetAllContentTypes();
        return listTypes.Result;
    }
    
    // GET Application/Edit
    [HttpGet]
    public IActionResult Edit(long idApplication)
    {
        
        if (idApplication == 0)
            return RedirectToActionByMessage("Index", "Dashboard",
                true, "Identificador da aplicação não informado.");

        var applicationEdit = apiService.GetApplication(idApplication);
        var application = applicationEdit.Result;
        if (application == null)
            return RedirectToActionByMessage("Index", "Dashboard",
                true, $"Aplicação com o ID: {idApplication} não encontrada ou desativada.");

        var model = new EditApplicationViewModel();
        model.IdApplication = application.Id;
        model.ApplicationCode = application.Code;
        model.ApplicationName = application.Name;
        model.ApplicationDescription = application.Description;
        model.AllowedFileTypes = string.Join(";", application.AllowedFileTypes
            .Select(s => s.ContentType).ToList());
        model.MaximumFileSizeMB = application.MaximumFileSize;
        model.CompressedFileContentToZip = application.CompressedFileContentToZip;
        model.ConvertImageFileToWebp = application.ConvertImageFileToWebp;
        model.ApplyOcrFileContent = application.ApplyOcrFileContent;
        model.AllowDuplicateFile = application.AllowDuplicateFile;
        model.RequiresFileReplication = application.RequiresFileReplication;
        
        return View(model);
        
    }
    
    // POST Application/SaveEdit
    [HttpPost]
    public IActionResult SaveEdit(EditApplicationViewModel model)
    {
        if (!ModelState.IsValid)
            return View("Edit", model); // volta para a mesma View com validação
        
        try
        {
            apiService.EditApplication(model);
        }
        catch (Exception error)
        {
            ModelState.AddModelError("_form", error.Message);
            return View("Edit", model);
        }
        
        return RedirectToActionByMessage("Index", "Dashboard", 
            false, "Aplicação atualizada com sucesso.");

    }
    
    // GET Application/SearchFile
    [HttpGet]
    public IActionResult SearchFile(string codeApplication, int pageNumber=1, 
        int recordsPerPage=15, bool isFilesSentForBackup=false, bool isFilesRemoved=false)
    {
        
        if (string.IsNullOrEmpty(codeApplication))
            return RedirectToActionByMessage("Index", "Dashboard",
                true, "Sigla da aplicação não informada.");

        var listFilsApplication = apiService.ListFilesApplication(codeApplication, pageNumber, 
            recordsPerPage, isFilesSentForBackup, isFilesRemoved).Result;
        var model = new ListFilesApplicationViewModel();
        
        return View(model);
        
    }
    
}