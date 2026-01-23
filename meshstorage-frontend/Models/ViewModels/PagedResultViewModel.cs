namespace meshstorage_frontend.Models.ViewModels;

public class PagedResultViewModel<T, TF>
{
    public int Page { get; set; }
    public int PageSize { get; set; }
    public long TotalRecords { get; set; }
    public TF? Filter { get; set; }
    public List<T> Items { get; set; } = [];
}