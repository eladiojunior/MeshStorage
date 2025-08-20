namespace meshstorage_frontend.Services.Cache;

public interface ICacheHelper
{
    IReadOnlyList<T> ListCache<T>(string key, Func<List<T>> factory);
}