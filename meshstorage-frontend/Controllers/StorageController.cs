using meshstorage_frontend.Helper;
using meshstorage_frontend.Services;
using Microsoft.AspNetCore.Mvc;

namespace meshstorage_frontend.Controllers;

public class StorageController(
    IApiService apiService,
    RazorViewToStringRenderer renderer,
    ILogger<HomeController> logger)
    : DefaultController(renderer, logger)
{
    
    // DELETE Storage/RemoveStorage
    [HttpDelete]
    public IActionResult RemoveStorage(long idServerStorage)
    {        
        try
        {
            apiService.RemoveStorage(idServerStorage);
            return JsonResultSucesso("Storage removido com sucesso");
        }
        catch (Exception error)
        {
            return TratarErroNegocio(error, $"RemoveStorage(idServerStorage={idServerStorage})");
        }
    }
}