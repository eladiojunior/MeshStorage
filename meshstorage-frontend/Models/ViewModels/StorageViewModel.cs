namespace meshstorage_frontend.Models.ViewModels;

public class StorageViewModel
{
    public int Id { get; set; }
    public string IdClient { get; set; }
    public string Name { get; set; }
    public string IpAddress { get; set; }
    public string OsName { get; set; }
    public int StorageCapacity { get; set; }
    public int StorageUsed { get; set; }
    public int FileCount { get; set; }
    public string Status { get; set; }
    public string LastConnected { get; set; } 
}