namespace meshstorage_frontend.Services.Exceptions;

public class ApiBusinessException(int code, string message) : Exception(message)
{
    public int Code { get; private set; } = code;
}