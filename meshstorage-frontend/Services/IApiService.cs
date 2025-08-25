using meshstorage_frontend.Models.ViewModels;

namespace meshstorage_frontend.Services;

public interface IApiService
{
    Task<SystemStatusViewModel> getSystemStatus();
    Task<List<StorageViewModel>> getStorages();
    Task<List<ApplicationViewModel>> getApplications();
    Task<ApplicationViewModel?> getApplication(long idApplication);
    Task<List<FileContentTypeViewModel>> getAllContentTypes();
    Task<ApplicationViewModel?> registreApplication(CreateApplicationViewModel model);
    Task<ApplicationViewModel?> editApplication(EditApplicationViewModel model);
    Task<ListFilesApplicationViewModel> listFilesApplication(string codeApplication, int pageNumber, 
        int recordsPerPage, bool isFilesSentForBackup, bool isFilesRemoved);
}