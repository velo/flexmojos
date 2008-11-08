package info.flexmojos.compile.test.report;

public class ErrorReport
{
    private String message;

    private String stackTrace;

    private String type;

    public String getMessage()
    {
        return message;
    }

    public String getStackTrace()
    {
        return stackTrace;
    }

    public String getType()
    {
        return type;
    }

    public void setMessage( String message )
    {
        this.message = message;
    }

    public void setStackTrace( String stackTrace )
    {
        this.stackTrace = stackTrace;
    }

    public void setType( String type )
    {
        this.type = type;
    }

}
