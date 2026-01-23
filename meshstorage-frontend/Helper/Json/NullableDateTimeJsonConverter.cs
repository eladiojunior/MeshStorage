using System.Globalization;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace meshstorage_frontend.Helper.Json;

public class NullableDateTimeJsonConverter : JsonConverter<DateTime?>
{
    private const string Format = "dd/MM/yyyy HH:mm:ss";

    public override DateTime? Read(ref Utf8JsonReader reader, Type typeToConvert, JsonSerializerOptions options)
    {
        if (reader.TokenType == JsonTokenType.Null)
            return null;

        var value = reader.GetString();

        if (string.IsNullOrWhiteSpace(value))
            return null;

        return DateTime.ParseExact(
            value,
            Format,
            CultureInfo.InvariantCulture
        );
    }

    public override void Write(Utf8JsonWriter writer, DateTime? value, JsonSerializerOptions options)
    {
        if (value.HasValue)
        {
            writer.WriteStringValue(
                value.Value.ToString(Format, CultureInfo.InvariantCulture)
            );
        }
        else
        {
            writer.WriteNullValue();
        }
    }
}