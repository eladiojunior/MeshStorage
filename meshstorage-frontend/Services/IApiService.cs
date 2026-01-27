using meshstorage_frontend.Models.Dto;
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
    Task<PagedResultViewModel<FileItemViewModel, FilterListFileViewModel>> ListFilesFilter(FilterListFileDto filter);
    void RemoveStorage(long idServerStorage);
    Task<(Stream Stream, string ContentType, string FileName)> DownloadFile(string idFile, string? userName, string? accessChannel);
    void RemoveFile(string idFile);
    Task<FileQrCodeViewModel?> GenerateLinkQrCodeFile(string idFile, long tokenExpirationTime, int maximumAccessesToken);
    Task<UploadFileInitViewModel> UploadFileInit(UploadFileInitDto uploadFileInit);
    Task<UploadFileOkViewModel> UploadFileSendChunk(UploadFileChunkDto uploadFileChunk);
    Task<UploadFileFinalizeViewModel> UploadFileFinalize(string uploadId);
    Task<UploadFileOkViewModel> UploadFileCancel(string uploadId);
}