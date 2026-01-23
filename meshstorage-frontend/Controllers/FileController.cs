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

        var filterSession = new FilterListFileDto
        {
            ApplicationCode = codeApplication,
            Page = 1,
            PageSize = 15
        };

        var model = apiService.ListFilesFilter(filterSession).Result;
        return View(model);
        
    }

    // POST File/SearchFileBuFilter
    [HttpPost]
    public IActionResult SearchFileByFilter(FilterListFileViewModel filter, int? page, int? pageSize)
    {
        
        var sessionFilter = new FilterListFileDto()
        {
            ApplicationCode = filter.ApplicationCode,
            FileLogicName = filter.FileLogicName,
            FileContentType = (filter.FileContentType!=null?filter.FileContentType.Split(";"): []),
            FilesRemoved = filter.FilesRemoved,
            FilesSentForBackup = filter.FilesSentForBackup,
            Page = page.HasValue?page.Value:1,
            PageSize = pageSize.HasValue? pageSize.Value:15,
            LastUpdated = DateTime.UtcNow
        };
        
        HttpContext.Session.SetObject(KeySessionEnum.FilterListFile.GetDescription(), sessionFilter);
        
        var model = apiService.ListFilesFilter(sessionFilter).Result;
        return View("SearchFile", model);
        
    }
    
}