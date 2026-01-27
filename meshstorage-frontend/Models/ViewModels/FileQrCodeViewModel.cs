using meshstorage_frontend.Helper;

namespace meshstorage_frontend.Models.ViewModels;

public class FileQrCodeViewModel
{
    public string IdFile { get; set; } = string.Empty;
    public string LinkAccessFile { get; set; } = string.Empty;
    public string QrCodeBase64 { get; set; } = string.Empty;
    public string ImgQrCodeBase64 => $"data:image/png;base64,{QrCodeBase64}";
    public long TokenExpirationTime { get; set; }
    public string TokenExpirationTimeFormatted =>
        (TokenExpirationTime switch
        {
            0 => "Nunca expira",
            1 => "Expira em 1 minuto",
            _ => $"Expira em {FormatterHelper.TimeInMinutesFormat(TokenExpirationTime)}"
        });
    public int MaximumAccessesToken { get; set; }
    public string MaximumAccessesTokenFormatted =>
        (MaximumAccessesToken switch
        {
            0 => "Não tem limite",
            1 => "Apenas 1 acesso",
            _ => $"{MaximumAccessesToken} acessos"
        });
    public DateTime DtRegisteredAccessFile { get; set; }
}