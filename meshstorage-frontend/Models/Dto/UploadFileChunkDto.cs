namespace meshstorage_frontend.Models.Dto;

public class UploadFileChunkDto
{
    public string UploadId { get; set; } = string.Empty;
    public byte[] ChunkData { get; set; } = Array.Empty<byte>();
    public int ChunkIndex { get; set; }
    public long TotalChunks { get; set; }
}