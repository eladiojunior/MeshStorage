using meshstorage_frontend.Models.ViewModels;

namespace meshstorage_frontend.Services;

public interface IApiService
{
    Task<SystemStatusViewModel> getSystemStatus();
    Task<List<StorageViewModel>> getStorages();
    Task<List<ApplicationViewModel>> getApplications();
}