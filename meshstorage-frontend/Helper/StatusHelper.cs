namespace meshstorage_frontend.Helper;

public class StatusHelper
{
    private static StatusHelper instance = null;
    private static readonly Dictionary<string, string> _statusClasses = new();
    private static readonly Dictionary<string, string> _iconsClasses = new();
    
    public static StatusHelper Get()
    {
        return instance ?? (instance = new StatusHelper());
    }

    private StatusHelper()
    {
        //Carregar status de Classes
        _statusClasses.Add("primary", "border-primary");
        _statusClasses.Add("success", "border-success");
        _statusClasses.Add("warning", "border-warning");
        _statusClasses.Add("danger",  "border-danger");
       
        //Carregar icons de Classes
        _iconsClasses.Add("primary", "text-primary");
        _iconsClasses.Add("success", "text-success");
        _iconsClasses.Add("warning", "text-warning");
        _iconsClasses.Add("danger",  "text-danger");    
    }

    public string GetStatusClasses(string status)
    {
        return _statusClasses[status];
    }

    public string GetIconsClasses(string status)
    {
        return _iconsClasses[status];
    }
    
}