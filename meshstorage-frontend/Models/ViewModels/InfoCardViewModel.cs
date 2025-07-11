using meshstorage_frontend.Helper;

namespace meshstorage_frontend.Models.ViewModels;

public class InfoCardViewModel
{
    public string Titulo { get; set; }
    public string Value { get; set; }
    public string Icon { get; set; }
    public string Status { get; set; } = "primary";
    public string Ajuda { get; set; }
    
    public InfoCardViewModel(string titulo, string value, string icon, string status, string ajuda)
    {
        Titulo = titulo;
        Value = value;
        Icon = icon;
        Status = status;
        Ajuda = ajuda;
    }
    
    public string StatusClasses => StatusHelper.Get().GetStatusClasses(Status);
    public string IconsClasses => StatusHelper.Get().GetIconsClasses(Status);
    
}