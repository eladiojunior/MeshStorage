using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.ModelBinding;
using Microsoft.AspNetCore.Mvc.Razor;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.AspNetCore.Mvc.ViewFeatures;

namespace meshstorage_frontend.Helper;

public class RazorViewToStringRenderer
{
    private readonly IRazorViewEngine _viewEngine;
    private readonly ITempDataProvider _tempDataProvider;
    private readonly IServiceProvider _serviceProvider;

    public RazorViewToStringRenderer(
        IRazorViewEngine viewEngine,
        ITempDataProvider tempDataProvider,
        IServiceProvider serviceProvider)
    {
        _viewEngine = viewEngine;
        _tempDataProvider = tempDataProvider;
        _serviceProvider = serviceProvider;
    }
    
    /// <summary>
    /// Renderizar a View em String para respostas Json;
    /// </summary>
    /// <param name="actionContext">Action contexto utilizado.</param>
    /// <param name="viewName">Nome da View a ser renderizada.</param>
    /// <param name="model">Informações da Model para carga.</param>
    /// <returns>View renderizada em String.</returns>
    public async Task<string> RenderViewToStringAsync(ActionContext actionContext, string viewName, object model)
    {
        var viewResult = _viewEngine.FindView(actionContext, viewName, false);

        if (!viewResult.Success)
            throw new InvalidOperationException($"View '{viewName}' não foi encontrada.");

        var viewDictionary = new ViewDataDictionary(new EmptyModelMetadataProvider(), new ModelStateDictionary())
        {
            Model = model
        };

        await using var sw = new StringWriter();
        var viewContext = new ViewContext(
            actionContext,
            viewResult.View,
            viewDictionary,
            new TempDataDictionary(actionContext.HttpContext, _tempDataProvider),
            sw,
            new HtmlHelperOptions()
        );

        await viewResult.View.RenderAsync(viewContext);
        return sw.ToString();
    }
    
}