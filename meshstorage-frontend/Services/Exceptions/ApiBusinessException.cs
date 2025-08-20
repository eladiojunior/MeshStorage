namespace meshstorage_frontend.Services.Exceptions;

public class ApiBusinessException : Exception
{
    public int Code { get; private set; }
    public ApiBusinessException(int code, string message) : base(message)
    {
        Code = code;
    }
}