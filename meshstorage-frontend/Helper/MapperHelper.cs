using System;
using System.Collections.Generic;
using meshstorage_frontend.Models.External;
using meshstorage_frontend.Models.ViewModels;

namespace meshstorage_frontend.Helper;

public class MapperHelper
{
    private static int FormatMbtoGb(int valueMb)
    {
        return (int)Math.Round((decimal)((valueMb / 1024) * 10), 0);
    }

    public static SystemStatusViewModel MapperSystemStatus(SystemStatusApiResponse? response)
    {
        if (response == null)
            return null;

        var model = new SystemStatusViewModel();
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

    public static List<StorageViewModel> MapperStorage(StorageApiResponse[]? response)
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

    public static List<ApplicationViewModel> MapperApplication(ApplicationApiResponse[]? response)
    {
        if (response == null)
            return null;
        var listModels = new List<ApplicationViewModel>();
        foreach (var item in response)
        {
            var itemModel = MapperApplication(item);
            if (itemModel != null)
                listModels.Add(itemModel);
        }
        return listModels;
    }
    
    private static StorageViewModel MapperStorage(StorageApiResponse? response)
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
        model.FileCount = 0;
        model.Status = response.Available ? "active" : "offline";
        model.LastConnected = response.DateTimeAvailable;
        
        return model;
    }
    private static ApplicationViewModel MapperApplication(ApplicationApiResponse? response)
    {
        if (response == null)
            return null;

        var model = new ApplicationViewModel();
        model.Id = response.Id;
        model.Name = response.ApplicationName;
        model.Description = response.ApplicationDescription;
        model.Icon = "apps";
        model.FileCount = response.TotalFiles;
       
        return model;
    }
}