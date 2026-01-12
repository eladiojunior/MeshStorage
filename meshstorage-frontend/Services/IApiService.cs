using meshstorage_frontend.Models.ViewModels;

namespace meshstorage_frontend.Services;

public interface IApiService
{
    Task<SystemStatusViewModel> GetSystemStatus();
    Task<List<StorageViewModel>> GetStorages();
    Task<List<ApplicationViewModel>> GetApplications();
    Task<ApplicationViewModel?> GetApplication(long idApplication);
    Task<List<FileContentTypeViewModel>> GetAllContentTypes();
    Task<ApplicationViewModel?> RegistreApplication(CreateApplicationViewModel model);
    Task<ApplicationViewModel?> EditApplication(EditApplicationViewModel model);
    Task<ListFilesApplicationViewModel> ListFilesApplication(string codeApplication, int pageNumber, 
        int recordsPerPage, bool isFilesSentForBackup, bool isFilesRemoved);
    void RemoveStorage(long idServerStorage);
}