namespace meshstorage_frontend.Models.Dto;

public class GenerateQrCodeDto
{
    public string IdFile { get; set; } = string.Empty;
    public long TokenExpirationTime { get; set; }
    public int MaximumAccessesToken { get; set; }
}