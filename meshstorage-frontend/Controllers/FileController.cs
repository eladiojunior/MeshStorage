using meshstorage_frontend.Helper;
using meshstorage_frontend.Models.Dto;
using meshstorage_frontend.Models.ViewModels;
using meshstorage_frontend.Services;
using Microsoft.AspNetCore.Mvc;

namespace meshstorage_frontend.Controllers;

public class FileController(
    IApiService apiService,
    RazorViewToStringRenderer renderer,
    ILogger<FileController> logger)
    : DefaultController(renderer)
{
    
    // GET File/SearchFile
    [HttpGet]
    public IActionResult SearchFile(string codeApplication)
    {
        
        if (string.IsNullOrEmpty(codeApplication))
            return RedirectToActionByMessage("Index", "Dashboard",
                true, "Sigla da aplicação não informada.");

        var filter = new FilterListFileViewModel
        {
            ApplicationCode = codeApplication
        };
        var pageNumber = 1;
        var recordsPerPage = 15;
        
        var filterSession = HttpContext.Session.GetObject<FilterListFileSession>
            (KeySessionEnum.FilterListFile.GetDescription());
        if (filterSession != null)
        {
            filter.ApplicationCode = filterSession.ApplicationCode;
            filter.FileLogicName = filterSession.FileLogicName;
            filter.FileContentType = filterSession.FileContentType;
            filter.FilesRemoved = filterSession.FilesRemoved;
            filter.FilesSentForBackup = filterSession.FilesSentForBackup;
            pageNumber = filterSession.Page;
            recordsPerPage = filterSession.PageSize;
        }
        
        var model = apiService.ListFilesFilter(filter, pageNumber, recordsPerPage).Result;
        return View(model);
        
    }

    // POST File/SearchFileBuFilter
    [HttpPost]
    public IActionResult SearchFileByFilter(FilterListFileViewModel filter, int pageNumber, int recordsPerPage)
    {
        
        try
        {
            
            var sessionFilter = new FilterListFileSession()
            {
                ApplicationCode = filter.ApplicationCode,
                FileLogicName = filter.FileLogicName,
                FileContentType = filter.FileContentType,
                FilesRemoved = filter.FilesRemoved,
                FilesSentForBackup = filter.FilesSentForBackup,
                Page = pageNumber,
                PageSize = recordsPerPage,
                LastUpdated = DateTime.UtcNow
            };
            
            HttpContext.Session.SetObject(KeySessionEnum.FilterListFile.GetDescription(), sessionFilter);
            
            var listFilesApplication = 
                apiService.ListFilesFilter(filter, pageNumber, recordsPerPage).Result;
            return JsonResultSucesso(RenderRazorViewToString("_ListFilesApplicationPartial", listFilesApplication), 
                "Filtro realizado com sucesso.");
            
        }
        catch (Exception error)
        {
            logger.LogError(error, "SearchFileByFilter(FilterListFileViewModel::filter, int::pageNumber, int::recordsPerPage)");
            return JsonResultErro(TratarErroNegocio(error, $"SearchFileByFilter()"));
        }
        
    }
    
}