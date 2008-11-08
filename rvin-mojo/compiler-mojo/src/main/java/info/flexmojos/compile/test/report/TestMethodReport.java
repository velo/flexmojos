package info.flexmojos.compile.test.report;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( "testMethodReport" )
public class TestMethodReport
{

    private ErrorReport error;

    private ErrorReport failure;

    private String name;

    private double time;

    public ErrorReport getError()
    {
        return error;
    }

    public ErrorReport getFailure()
    {
        return failure;
    }

    public String getName()
    {
        return name;
    }

    public double getTime()
    {
        return time;
    }

    public void setError( ErrorReport error )
    {
        this.error = error;
    }

    public void setFailure( ErrorReport failure )
    {
        this.failure = failure;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public void setTime( double time )
    {
        this.time = time;
    }

}
