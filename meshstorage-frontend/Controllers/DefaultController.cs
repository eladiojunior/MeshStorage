using System.Collections;
using System.Text.Json;
using meshstorage_frontend.Exceptions;
using meshstorage_frontend.Helper;
using meshstorage_frontend.Services.Exceptions;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.ModelBinding;

namespace meshstorage_frontend.Controllers;

public class DefaultController(RazorViewToStringRenderer renderer, 
    ILogger<HomeController> logger) : Controller
{
    /// <summary>
    /// Cria um retorno Json de Erro, Tipo=ERROR, com mensagem de erro.
    /// </summary>
    /// <param name="mensagemErro">Mensage de erro para apresentação.</param>
    /// <returns></returns>
    internal JsonResult JsonResultError(string mensagemErro)
    {
        return Json(new { Tipo = ResponseMessageTypeEnum.Error.GetDescription(), Erros = new List<string> { mensagemErro } }, JsonSerializerOptions.Default);
    }

    /// <summary>
    /// Cria um retorno Json de Alert, ResponseType=INFO, com mensagem de alerta.
    /// </summary>
    /// <param name="mensagemAlerta">Mensage de alerta para apresentação.</param>
    /// <returns></returns>
    internal JsonResult JsonResultAlerta(string mensagemAlerta)
    {
        return Json(new { Tipo = ResponseMessageTypeEnum.Alert.GetDescription(), Mensagem = mensagemAlerta }, JsonSerializerOptions.Default);
    }

    /// <summary>
    ///     Cria um retorno Json de Erro (Result = false) com mensagem de erro.
    /// </summary>
    /// <param name="mensagensErro">Lista de mensagens de erro que deve ser apresentada ao usuário.</param>
    /// <returns></returns>
    internal JsonResult JsonResultErro(IEnumerable mensagensErro)
    {
        return Json(new { Tipo = ResponseMessageTypeEnum.Error.GetDescription(), Erros = mensagensErro }, JsonSerializerOptions.Default);
    }

    internal JsonResult JsonResultErro(object model, string mensagem = "")
    {
        return Json(new { Tipo = ResponseMessageTypeEnum.Error.GetDescription(), Model = model, Mensagem = mensagem }, JsonSerializerOptions.Default);
    }

    internal JsonResult JsonResultErro(Exception ex)
    {
        return Json(new { Tipo = ResponseMessageTypeEnum.Error.GetDescription(), Erros = new List<string> { ex.Message } }, JsonSerializerOptions.Default);
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
                    Tipo = ResponseMessageTypeEnum.Error.GetDescription(),
                    Chaves = chaves,
                    Erros = mensagens.Where(a => a != null).Select(a => a.ErrorMessage).ToList()
                },
                JsonSerializerOptions.Default);
    }

    /// <summary>
    ///     Cria um retorno Json de Sucesso (Tipo = INFO) com mensagem para o usuário (opcional).
    /// </summary>
    /// <param name="mensagemInfo">Mensagem de informação que deve ser apresentada ao usuário.</param>
    /// <returns></returns>
    internal JsonResult JsonResultSucesso(string mensagemInfo = "")
    {
        return Json(new { Tipo = ResponseMessageTypeEnum.Info.GetDescription(), Mensagem = mensagemInfo }, JsonSerializerOptions.Default);
    }

    /// <summary>
    ///     Cria um retorno Json de Sucesso (Tipo = INFO) com Model e mensagem para o usuário (opcional).
    /// </summary>
    /// <param name="model">Informações do Model para renderizar a view.</param>
    /// <param name="mensagemInfo">Mensagem de alerta que deve ser apresentada ao usuário.</param>
    /// <returns></returns>
    internal JsonResult JsonResultSucesso(object model, string mensagemInfo = "")
    {
        return Json(new { Tipo = ResponseMessageTypeEnum.Info.GetDescription(), Model = model, Mensagem = mensagemInfo }, JsonSerializerOptions.Default);
    }

    /// <summary>
    ///     Renderizar a View em String para respostas Json;
    /// </summary>
    /// <param name="viewName">Nome da View a ser renderizada.</param>
    /// <param name="model">Informações da Model para carga.</param>
    /// <returns></returns>
    internal string RenderRazorViewToString(string viewName, object model)
    {
        var html = renderer.RenderViewToStringAsync(ControllerContext, viewName, model);
        return html.Result;
    }

    /// <summary>
    ///     Tratar as mensagens de erro de negócio.
    /// </summary>
    /// <param name="erro">Exception de erro para tratamento.</param>
    /// <param name="localErro">Local do erro.</param>
    internal JsonResult TratarErroNegocio(Exception erro, string? localErro = null)
    {
        var mensagem = $"{(string.IsNullOrEmpty(localErro) ? localErro + ": " : "")} Erro inesperado na requisição.";
        var tipo = ResponseMessageTypeEnum.Error;
        erro = UnwrapException(erro);
        switch (erro)
        {
            case ApiBusinessException apiEx:
                mensagem = apiEx.Message;
                tipo = ResponseMessageTypeEnum.Alert;
                break;
            case BusinessException bizEx:
                mensagem = bizEx.Message;
                tipo = ResponseMessageTypeEnum.Alert;
                break;
        }
        return tipo == ResponseMessageTypeEnum.Error ? JsonResultErro(mensagem) : JsonResultAlerta(mensagem);
    }

    /// <summary>
    /// Verifica se existe uma InnerException no AggregateException.
    /// </summary>
    /// <param name="ex">Exception a ser verificada.</param>
    /// <returns></returns>
    private Exception UnwrapException(Exception ex)
    {
        if (ex is AggregateException agg)
            return agg.Flatten().InnerExceptions.First();
        return ex;
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