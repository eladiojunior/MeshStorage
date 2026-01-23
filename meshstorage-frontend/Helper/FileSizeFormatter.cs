namespace meshstorage_frontend.Helper;

public static class FileSizeFormatter
{
    public static string Format(long bytes)
    {
        if (bytes < 1024)
            return $"{bytes} B";
        var kb = bytes / 1024d;
        if (kb < 1024)
            return $"{kb:N1} KB";
        var mb = kb / 1024d;
        if (mb < 1024)
            return $"{mb:N1} MB";
        var gb = mb / 1024d;
        return $"{gb:N2} GB";
    }
}