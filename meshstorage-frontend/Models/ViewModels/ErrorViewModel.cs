namespace meshstorage_frontend.Models.ViewModels;

public class ErrorViewModel(string? code, string message)
{
    public string? Code { get; set; } = code;
    public string? Message { get; set; } = message;

    public bool ShowRequestId => !string.IsNullOrEmpty(Code);
}