using System.Collections;
using System.Text.Json;
using meshstorage_frontend.Exceptions;
using meshstorage_frontend.Helper;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.ModelBinding;

namespace meshstorage_frontend.Controllers;

public class DefaultController : Controller
{
    private readonly RazorViewToStringRenderer _renderer;
    private readonly ILogger<HomeController> _logger;
    
    public DefaultController(RazorViewToStringRenderer renderer, ILogger<HomeController> logger)
    {
        _renderer = renderer;
        _logger = logger;
    }
    
    /// <summary>
    /// Cria um retorno Json de Erro (Result = false) com mensagem de erro.
    /// </summary>
    /// <param name="mensagemErro"></param>
    /// <returns></returns>
    internal JsonResult JsonResultErro(string mensagemErro)
    {
        return Json(new { HasErro = true, Erros = new List<string> { mensagemErro } }, JsonSerializerOptions.Default);
    }

    /// <summary>
    ///     Cria um retorno Json de Erro (Result = false) com mensagem de erro.
    /// </summary>
    /// <param name="mensagensErro">Lista de mensagens de erro que deve ser apresentada ao usuário.</param>
    /// <returns></returns>
    internal JsonResult JsonResultErro(IEnumerable mensagensErro)
    {
        return Json(new { HasErro = true, Erros = mensagensErro }, JsonSerializerOptions.Default);
    }

    internal JsonResult JsonResultErro(object model, string mensagem = "")
    {
        return Json(new { HasErro = true, Model = model, Mensagem = mensagem }, JsonSerializerOptions.Default);
    }

    internal JsonResult JsonResultErro(Exception ex)
    {
        return Json(new { HasErro = true, Erros = new[] { ex.Message } }, JsonSerializerOptions.Default);
    }

    /// <summary>
    ///     Cria um retorno Json de Erro (Result = false) com mensagem de erro, com base nos erros do modelState
    /// </summary>
    /// <param name="modelState">Lista de mensagens de erro que deve ser apresentada ao usuário.</param>
    /// <returns></returns>
    internal JsonResult JsonResultErro(ModelStateDictionary modelState)
    {
        var chaves = from modelstate in modelState.AsQueryable().Where(f => f.Value.Errors.Count > 0)
            select modelstate.Key;
        var mensagens =
            from modelstate in modelState.AsQueryable().Where(f => f.Value.Errors.Count > 0)
            select modelstate.Value.Errors.FirstOrDefault(a => !string.IsNullOrEmpty(a.ErrorMessage));
        return
            Json(
                new
                {
                    HasErro = true,
                    Chaves = chaves,
                    Erros = mensagens.Where(a => a != null).Select(a => a.ErrorMessage).ToList()
                },
                JsonSerializerOptions.Default);
    }

    /// <summary>
    ///     Cria um retorno Json de Sucesso (Result = true) com mensagem para o usuário (opcional).
    /// </summary>
    /// <param name="mensagemAlerta">Mensagem de alerta que deve ser apresentada ao usuário.</param>
    /// <returns></returns>
    internal JsonResult JsonResultSucesso(string mensagemAlerta = "")
    {
        return Json(new { HasErro = false, Mensagem = mensagemAlerta }, JsonSerializerOptions.Default);
    }

    /// <summary>
    ///     Cria um retorno Json de Sucesso (Result = true) com Model e mensagem para o usuário (opcional).
    /// </summary>
    /// <param name="model">Informações do Model para renderizar a view.</param>
    /// <param name="mensagemAlerta">Mensagem de alerta que deve ser apresentada ao usuário.</param>
    /// <returns></returns>
    internal JsonResult JsonResultSucesso(object model, string mensagemAlerta = "")
    {
        return Json(new { HasErro = false, Model = model, Mensagem = mensagemAlerta }, JsonSerializerOptions.Default);
    }

    /// <summary>
    ///     Renderizar a View em String para respostas Json;
    /// </summary>
    /// <param name="viewName">Nome da View a ser renderizada.</param>
    /// <param name="model">Informações da Model para carga.</param>
    /// <returns></returns>
    internal string RenderRazorViewToString(string viewName, object model)
    {
        var html = _renderer.RenderViewToStringAsync(ControllerContext, viewName, model);
        return html.Result;
    }

    /// <summary>
    ///     Tratar as mensagens de erro de negócio.
    /// </summary>
    /// <param name="erro">Exception de erro para tratamento.</param>
    /// <param name="localErro">Local do erro.</param>
    internal string TratarErroNegocio(Exception erro, string localErro = null)
    {
        if (erro.GetType() == typeof(BusinessException))
        {
            var message = erro.Message;
            if (!string.IsNullOrEmpty(message))
                return message;
        }
        _logger.LogError("Error: {}", erro.Message);
        return $"{(string.IsNullOrEmpty(localErro) ? localErro + ": " : "")} Erro inesperado na requisição.";
    }

    /// <summary>
    /// Controle de Redirect com registro de mensagem de sucesso ou erro na TempData para recuperação na
    /// interface (View) que receberá a requisição.
    /// </summary>
    /// <param name="actionName">Nome da Action acionada.</param>
    /// <param name="controllerName">Nome da Controller acionada</param>
    /// <param name="hasError">Flag para apresentar mensagem de erro ou sucesso.</param>
    /// <param name="message">Mensagem a se apresentada</param>
    /// <returns></returns>
    internal RedirectToActionResult RedirectToActionByMessage(string? actionName, 
        string? controllerName, bool hasError, string message)
    {
        if (hasError) TempData["MessageError"] = message;
        else TempData["MessageSuccess"] = message;
        return RedirectToAction(actionName, controllerName);
    }
    
}