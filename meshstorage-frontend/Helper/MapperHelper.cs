using meshstorage_frontend.Models.External;
using meshstorage_frontend.Models.External.Request;
using meshstorage_frontend.Models.External.Response;
using meshstorage_frontend.Models.ViewModels;

namespace meshstorage_frontend.Helper;

public class MapperHelper
{

    private int FormatMbtoGb(long valueMb)
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
        if (response == null)
            return null;
        var listModels = new List<StorageViewModel>();
        foreach (var item in response)
        {
            var itemModel = MapperStorage(item);
            if (itemModel != null)
                listModels.Add(itemModel);
        }
        return listModels;
    }

    public List<ApplicationViewModel> MapperApplication(ApplicationApiResponse[]? response,
        List<FileContentTypeViewModel> allContentTypes)
    {
        if (response == null)
            return null;
        var listModels = new List<ApplicationViewModel>();
        foreach (var item in response)
        {
            var itemModel = MapperApplication(item, allContentTypes);
            if (itemModel != null)
                listModels.Add(itemModel);
        }
        return listModels;
    }
    
    private StorageViewModel MapperStorage(StorageApiResponse? response)
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
        request.MaximumFileSize = model.MaximumFileSizeMB;
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
        request.MaximumFileSize = model.MaximumFileSizeMB;
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

        var model = new ApplicationViewModel();
        model.Id = response.Id;
        model.Code = response.ApplicationCode;
        model.Name = response.ApplicationName;
        model.Description = response.ApplicationDescription;
        model.Icon = "apps";
        model.MaximumFileSize = response.MaximumFileSize;
        model.CompressedFileContentToZip = response.CompressedFileContentToZip;
        model.ConvertImageFileToWebp = response.ConvertImageFileToWebp;
        model.ApplyOcrFileContent = response.ApplyOcrFileContent;
        model.AllowDuplicateFile = response.AllowDuplicateFile;
        model.RequiresFileReplication = response.RequiresFileReplication;
        model.TotalFiles = response.TotalFiles;
        model.AllowedFileTypes = MapperAllowedFileTypes(response.AllowedFileTypes, allContentTypes);
        return model;
    }

    /// <summary>
    /// Mapear lista de ContentType em objeto de FileContentType para apresentar na aplicação.
    /// </summary>
    /// <param name="responseAllowedFileTypes">Lista de ContentType simples (string).</param>
    /// <returns></returns>
    private List<FileContentTypeViewModel> MapperAllowedFileTypes(string[] responseAllowedFileTypes, 
        List<FileContentTypeViewModel> allContentTypes)
    {
        var result = new List<FileContentTypeViewModel>();
        foreach (var contentType in responseAllowedFileTypes)
        {
            var item = allContentTypes
                .FirstOrDefault(f => f.ContentType.Equals(contentType));
            if (item != null && !result.Contains(item))
                result.Add(item);
        }
        return result;
    }

    public List<FileContentTypeViewModel> MapperFileContentType(FileContentTypeApiResponse[]? response)
    {
        if (response == null)
            return null;
        var listModels = new List<FileContentTypeViewModel>();
        foreach (var item in response)
        {
            var itemModel = MapperFileContentType(item);
            if (itemModel != null)
                listModels.Add(itemModel);
        }
        return listModels;
    }
    
    private FileContentTypeViewModel MapperFileContentType(FileContentTypeApiResponse? response)
    {
        if (response == null)
            return null;

        var model = new FileContentTypeViewModel();
        model.Code = response.Code;
        model.NameEnum = response.NameEnum;
        model.Extension = response.Extension;
        model.Description = response.Description;
        model.ContentType = response.ContentType;
       
        return model;
    }
    
}