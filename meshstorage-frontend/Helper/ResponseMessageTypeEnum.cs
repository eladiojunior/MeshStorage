using System.ComponentModel;

namespace meshstorage_frontend.Helper;

public enum ResponseMessageTypeEnum
{
    [Description("ERROR")]
    Error = 1,
    [Description("ALERT")]
    Alert = 2,
    [Description("INFO")]
    Info = 3
}