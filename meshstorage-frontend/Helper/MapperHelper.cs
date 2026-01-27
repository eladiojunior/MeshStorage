using meshstorage_frontend.Models.Dto;
using meshstorage_frontend.Models.External.Request;
using meshstorage_frontend.Models.External.Response;
using meshstorage_frontend.Models.ViewModels;

namespace meshstorage_frontend.Helper;

public class MapperHelper
{
    private static int FormatMbtoGb(long valueMb)
    {
        return (int)Math.Round((decimal)(valueMb / 1024), 0);
    }

    public SystemStatusViewModel MapperSystemStatus(SystemStatusApiResponse? response)
    {
        var model = new SystemStatusViewModel();
        if (response == null)
            return model;

        var totalSpase = response.TotalSpaceStorages;
        var totalFreed = response.TotalFreeStorages;
        var totalUsed = totalSpase - totalFreed;
        var formattedStorage = $"{FormatMbtoGb(totalSpase)}/{FormatMbtoGb(totalUsed)} GB";
        model.TotalStorage = formattedStorage;
        model.ConnectedClients = response.TotalClientsConnected;
        model.TotalFiles = response.TotalFilesStorages;
        var statusHealth = response.SystemHealth 
            switch
        {
            "healthy" => "Saudável",
            "warning" => "Atenção",
            _ => "Erro"
        };
        model.Health = statusHealth;
        model.MessageStatus = response.MessageStatus;
        var statusSystem = response.SystemHealth 
            switch
            {
                "healthy" => "success",
                "warning" => "warning",
                _ => "danger"
            };
        model.Status = statusSystem;
        return model;
    }

    public List<StorageViewModel> MapperStorage(StorageApiResponse[]? response)
    {
        var listModels = new List<StorageViewModel>();
        if (response == null)
            return listModels;

        listModels.AddRange(response.Select(MapperStorage).OfType<StorageViewModel>());
        return listModels;
    }

    public List<ApplicationViewModel> MapperApplication(ApplicationApiResponse[]? response, 
        List<FileContentTypeViewModel> allContentTypes)
    {
        
        var listModels = new List<ApplicationViewModel>();
        if (response == null)
            return listModels;

        listModels.AddRange(response.Select(appResponse => 
            MapperApplication(appResponse, allContentTypes)).OfType<ApplicationViewModel>());
        
        return listModels;
    }
    
    private StorageViewModel? MapperStorage(StorageApiResponse? response)
    {
        if (response == null)
            return null;

        var model = new StorageViewModel();
        var total = response.TotalSpace;
        var free = response.FreeSpace;
        var used = total - free;

        model.Id = response.Id;
        model.IdClient = response.IdClient;
        model.Name = $"{response.StorageName} [{response.ServerName}]";
        model.IpAddress = response.IpServer;
        model.OsName = response.OsServer;
        model.StorageCapacity = FormatMbtoGb(response.TotalSpace);
        model.StorageUsed = FormatMbtoGb(used);
        model.FileCount = response.TotalFiles;
        model.Status = response.StatusCode == 1 ? "active" : "offline";
        
        return model;
    }

    public CreateApplicationApiRequest MapperApplication(CreateApplicationViewModel? model)
    {
        var request = new CreateApplicationApiRequest();
        if (model == null)
            return request;
        
        request.ApplicationCode = model.ApplicationCode;
        request.ApplicationName = model.ApplicationName;
        request.ApplicationDescription = model.ApplicationDescription;
        request.MaximumFileSize = model.MaximumFileSizeMb;
        request.AllowedFileTypes = model.AllowedFileTypes.Split(";");
        request.CompressedFileContentToZip = model.CompressedFileContentToZip;
        request.ConvertImageFileToWebp = model.ConvertImageFileToWebp;
        request.ApplyOcrFileContent = model.ApplyOcrFileContent;
        request.AllowDuplicateFile = model.AllowDuplicateFile;
        request.RequiresFileReplication = model.RequiresFileReplication;
        
        return request;
        
    }
    public UpdateApplicationApiRequest MapperApplication(EditApplicationViewModel? model)
    {
        var request = new UpdateApplicationApiRequest();
        if (model == null)
            return request;
        
        request.ApplicationCode = model.ApplicationCode;
        request.ApplicationName = model.ApplicationName;
        request.ApplicationDescription = model.ApplicationDescription;
        request.MaximumFileSize = model.MaximumFileSizeMb;
        request.AllowedFileTypes = model.AllowedFileTypes.Split(";");
        request.CompressedFileContentToZip = model.CompressedFileContentToZip;
        request.ConvertImageFileToWebp = model.ConvertImageFileToWebp;
        request.ApplyOcrFileContent = model.ApplyOcrFileContent;
        request.AllowDuplicateFile = model.AllowDuplicateFile;
        request.RequiresFileReplication = model.RequiresFileReplication;
        
        return request;
        
    }
    
    public ApplicationViewModel? MapperApplication(ApplicationApiResponse? response, 
        List<FileContentTypeViewModel> allContentTypes)
    {
        if (response == null)
            return null;

        var model = new ApplicationViewModel
        {
            Id = response.Id,
            Code = response.ApplicationCode,
            Name = response.ApplicationName,
            Description = response.ApplicationDescription,
            Icon = "apps",
            MaximumFileSize = response.MaximumFileSize,
            CompressedFileContentToZip = response.CompressedFileContentToZip,
            ConvertImageFileToWebp = response.ConvertImageFileToWebp,
            ApplyOcrFileContent = response.ApplyOcrFileContent,
            AllowDuplicateFile = response.AllowDuplicateFile,
            RequiresFileReplication = response.RequiresFileReplication,
            TotalFiles = response.TotalFiles,
            AllowedFileTypes = MapperAllowedFileTypes(response.AllowedFileTypes, allContentTypes)
        };
        return model;
    }

    /// <summary>
    /// Mapear lista de ContentType em objeto de FileContentType para apresentar na aplicação.
    /// </summary>
    /// <param name="responseAllowedFileTypes">Lista de ContentType simples (string).</param>
    /// <param name="allContentTypes">Lista de Tipos de arquivos para verificação.</param>
    /// <returns></returns>
    private List<FileContentTypeViewModel> MapperAllowedFileTypes(string[] responseAllowedFileTypes,
        List<FileContentTypeViewModel> allContentTypes)
    {
        var result = new List<FileContentTypeViewModel>();
        foreach (var contentType in responseAllowedFileTypes)
        {
            var item = GetContentType(contentType, allContentTypes);
            if (item != null && !result.Contains(item))
                result.Add(item);
        }
        return result;
    }

    public List<FileContentTypeViewModel> MapperFileContentType(FileContentTypeApiResponse[]? response)
    {
        var listModels = new List<FileContentTypeViewModel>();
        if (response == null)
            return listModels;
        listModels.AddRange(response.Select(MapperFileContentType).OfType<FileContentTypeViewModel>());
        return listModels;
    }
    
    private FileContentTypeViewModel? MapperFileContentType(FileContentTypeApiResponse? response)
    {
        if (response == null)
            return null;

        var model = new FileContentTypeViewModel
        {
            Code = response.Code,
            NameEnum = response.NameEnum,
            Extension = response.Extension,
            Description = response.Description,
            ContentType = response.ContentType
        };

        return model;
    }
    
    public PagedResultViewModel<FileItemViewModel, FilterListFileViewModel> MapperListFile
        (ListFilesApiResponse? response, List<FileContentTypeViewModel> allContentTypes)
    {
        var model = new PagedResultViewModel<FileItemViewModel, FilterListFileViewModel>
        {
            TotalRecords = 0,
            Filter = null,
            Page = 1,
            PageSize = 15,
            Items = []
        };

        if (response == null)
            return model;

        model.TotalRecords = response.TotalRecords;
        if (model.TotalRecords == 0) 
            return model;

        foreach (var fileModel in response.Files.Select(fileResponse => 
                     MapperFileItem(fileResponse, allContentTypes)).OfType<FileItemViewModel>())
            model.Items.Add(fileModel);
        
        return model;
        
    }

    private FileItemViewModel? MapperFileItem(FileItemResponse? response, 
        List<FileContentTypeViewModel> allContentTypes)
    {
        if (response == null)
            return null;
        
        var model = new FileItemViewModel
        {
            IdFile = response.IdFile,
            FileLogicName = response.FileLogicName,
            FileFisicalName = response.FileFisicalName,
            FileContentType = response.FileContentType,
            FileExtension = GetFileExtension(response.FileContentType, allContentTypes),
            FileLength = response.FileLength,
            HashFileBytes = response.HashFileBytes,
            CompressedFileContent = response.CompressedFileContent,
            DtRegisteredFileStorage = response.DateTimeRegisteredFileStorage,
            FileStatusDescription = response.FileStatusDescription,
            PercentualCompressedFile = (response is { CompressedFileContent: true, FileCompressed: not null }?
                response.FileCompressed.PercentualCompressedFile:0)
        };

        return model;
        
    }

    /// <summary>
    /// Recupera um FileContentType pelo seu contentType informado.
    /// </summary>
    /// <param name="contentType">Tipo do arquivo para recuperar todas as informações.</param>
    /// <param name="allContentTypes">Tipos de arquivos para recuperação.</param>
    /// <returns></returns>
    private FileContentTypeViewModel? GetContentType(string contentType, List<FileContentTypeViewModel> allContentTypes)
    {
        var resultContentType = allContentTypes
            .FirstOrDefault(f => f.ContentType.Equals(contentType));
        return resultContentType;
    }

    /// <summary>
    /// Recupera a extensão (.pdf, .doc. .jpj etc.) a partir de um ContentType informado.
    /// </summary>
    /// <param name="contentType">Tipo do arquivo para recuperar a extenção.</param>
    /// <param name="allContentTypes">Lista de tipos de arquivos para verificação</param>
    /// <returns></returns>
    private string GetFileExtension(string contentType, List<FileContentTypeViewModel> allContentTypes)
    {
        var resultContentType = GetContentType(contentType, allContentTypes);
        return resultContentType == null ? "" : resultContentType.Extension;
    }

    public FilterListFileViewModel MapperFilterListFile(FilterListFileDto filter)
    {
        var model = new FilterListFileViewModel
        {
            ApplicationCode = filter.ApplicationCode,
            FileLogicName = filter.FileLogicName,
            FileContentType = string.Join(";", filter.FileContentType),
            FilesRemoved = filter.FilesRemoved,
            FilesSentForBackup = filter.FilesSentForBackup
        };
        return model;
    }

    public FileQrCodeViewModel? MapperGenerateQrCodeFile(GerenateQrCodeFileResponse? response)
    {
        if (response == null)
            return null;
        
        var model = new FileQrCodeViewModel()
        {
            IdFile = response.IdFile,
            LinkAccessFile = response.LinkAccessFile,
            QrCodeBase64 = response.Base64QrCodeAccessFile,
            DtRegisteredAccessFile = response.DateTimeRegisteredAccessFile,
            MaximumAccessesToken = 0,
            TokenExpirationTime = 0
        };
        return model;
    }

    public UploadFileInitApiRequest MapperUploadFileInit(UploadFileInitDto dto)
    {
        return new UploadFileInitApiRequest
        {
            ApplicationCode = dto.ApplicationCode,
            FileName = dto.FileName,
            ContentType = dto.ContentType,
            FileSize = dto.FileSize
        };
    }
    public UploadFileInitViewModel? MapperUploadFileInit(UploadFileInitApiResponse? response)
    {
        if (response == null)
            return null;
        return new UploadFileInitViewModel
        {
            UploadId = response.UploadId,
            ChunkSize = response.ChunkSize,
            TotalChunks = response.ChunkTotal
        };
    }

    public UploadFileFinalizeViewModel? MapperUploadFileFinalize(UploadFileFinalizeApiResponse? response)
    {
        if (response == null)
            return null;
        return new UploadFileFinalizeViewModel
        {
            IdFile = response.IdFile,
            Status = response.Status
        };
    }
}