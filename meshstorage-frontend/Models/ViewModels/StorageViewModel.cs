namespace meshstorage_frontend.Models.ViewModels;

public class StorageViewModel
{
    public long Id { get; set; }
    public string IdClient { get; set; }
    public string Name { get; set; }
    public string IpAddress { get; set; }
    public string OsName { get; set; }
    public long StorageCapacity { get; set; }
    public long StorageUsed { get; set; }
    public long FileCount { get; set; }
    public string Status { get; set; }

    public bool IsOffline
    {
        get
        {
            return Status.Equals("offline");
        }
    }

    public string StatusLabel
    {
        get
        {
            var statusLegenda = (Status.Equals("active") ? "Ativo - Online" : 
                Status.Equals("warning") ? "Atenção" : 
                IsOffline ? "Desativado" : "Erro");
            return statusLegenda;
        }
    }

    private double PercentualStorageUsage {
        get
        {
            if (StorageUsed == 0 || StorageCapacity == 0) 
                return 0;
            var percentual = ((double)StorageUsed / StorageCapacity);
            return percentual;
        }
    }
    public string StorageUsage
    {
        get
        {
            return PercentualStorageUsage.ToString("P1");
        }
    }

    public string StatusBgClasses
    {
        get
        {
            return (Status.Equals("active") ? "bg-success" : 
                Status.Equals("warning") ? "bg-warning" : 
                IsOffline ? "bg-secondary" : "bg-danger");            
        }
    }

    public string StatusBgProgressBarClasses
    {
        get
        {
            var isWarning = PercentualStorageUsage >= 0.75; //75%
            var isError = PercentualStorageUsage >= 0.90; //90%
            return (IsOffline ? "bg-secondary" : isError ? "bg-danger" : isWarning ? "bg-warning" : "bg-primary");
        }
    }

    public string StorageUsageProgressBar
    {
        get
        {
            return $"{Math.Round(PercentualStorageUsage * 100, 0)}%";
        }
    }
}