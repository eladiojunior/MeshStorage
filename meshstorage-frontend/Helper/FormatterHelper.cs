namespace meshstorage_frontend.Helper;

public static class FormatterHelper
{
    /**
     * Converte os bytes em um formato Bytes, KB, MB ou GB.
     */
    public static string FileSizeFormat(long bytes)
    {
        string[] sizes = ["Bytes", "KB", "MB", "GB"];
        double len = bytes;
        var order = 0;
        while (len >= 1024 && order < sizes.Length - 1)
        {
            order++;
            len /= 1024;
        }
        return $"{len:0.##} {sizes[order]}";
    }

    /**
     * Converte o tempo (em minutes) em minutos, semanas, dias e anos.
     */
    public static string TimeInMinutesFormat(long totalMinutes)
    {
        if (totalMinutes <= 0)
            return "0 minutos";

        const int minutesPerHour = 60;
        const int minutesPerDay = 1440;      // 60 * 24
        const int minutesPerYear = 525600;   // 60 * 24 * 365

        var years = totalMinutes / minutesPerYear;
        var remaining = totalMinutes % minutesPerYear;

        var days = remaining / minutesPerDay;
        remaining %= minutesPerDay;

        var hours = remaining / minutesPerHour;
        var minutes = remaining % minutesPerHour;

        var parts = new List<string>();

        if (years > 0)
            parts.Add($"{years} {(years == 1 ? "ano" : "anos")}");

        if (days > 0)
            parts.Add($"{days} {(days == 1 ? "dia" : "dias")}");

        if (hours > 0)
            parts.Add($"{hours} {(hours == 1 ? "hora" : "horas")}");

        if (minutes > 0)
            parts.Add($"{minutes} {(minutes == 1 ? "minuto" : "minutos")}");

        return string.Join(" ", parts);
    }
}