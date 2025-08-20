using Microsoft.Extensions.Caching.Memory;

namespace meshstorage_frontend.Services.Cache;

public class CacheHelper: ICacheHelper
{
    public const string CacheContentTypeKey = "cache-content-types";

    private readonly IMemoryCache _cache;
    private readonly MemoryCacheEntryOptions _options;
    private readonly SemaphoreSlim _lock = new(1, 1); // evita stampede

    public CacheHelper(IMemoryCache cache)
    {
        _cache = cache;
        _options = new MemoryCacheEntryOptions
        {
            AbsoluteExpirationRelativeToNow = TimeSpan.FromHours(4), // TTL padrão de 4h
            SlidingExpiration = TimeSpan.FromHours(1), // opcional: renova se houver acesso
            Size = 1 // use Size se limitar total do cache
        };
    }

    public IReadOnlyList<T> ListCache<T>(string key, Func<List<T>> factory)
    {
        
        if (_cache.TryGetValue(key, out IReadOnlyList<T>? cached) && cached is not null)
            return cached;
            
        _lock.WaitAsync();
        
        try
        {
            //Tentar novamente...
            if (_cache.TryGetValue(key, out cached) && cached is not null)
                return cached;

            var fresh = factory();
            var result = fresh;
            if (result.Count != 0)
            {//Guardar no cache...
                _cache.Set(key, result, _options);
            }
            return result;
            
        }
        finally
        {
            _lock.Release();
        }
    }

}