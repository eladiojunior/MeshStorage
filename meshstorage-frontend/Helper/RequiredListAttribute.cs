using System.ComponentModel.DataAnnotations;

namespace meshstorage_frontend.Helper;

public class RequiredListAttribute : ValidationAttribute
{
    public override bool IsValid(object? value)
    {
        if (value == null) return false;
        var list = value as IList<string>;
        return list != null && list.Any() && list.All(x => !string.IsNullOrWhiteSpace(x));
    }
}