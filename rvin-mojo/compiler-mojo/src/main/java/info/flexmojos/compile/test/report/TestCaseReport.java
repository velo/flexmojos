package info.flexmojos.compile.test.report;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( "testCaseReport" )
public class TestCaseReport
{

    private int errors;

    private int failures;

    private TestMethodReport[] methods;

    private String name;

    private int tests;

    private double time;

    public int getErrors()
    {
        return errors;
    }

    public int getFailures()
    {
        return failures;
    }

    public TestMethodReport[] getMethods()
    {
        return methods;
    }

    public String getName()
    {
        return name;
    }

    public int getTests()
    {
        return tests;
    }

    public double getTime()
    {
        return time;
    }

    public void setErrors( int errors )
    {
        this.errors = errors;
    }

    public void setFailures( int failures )
    {
        this.failures = failures;
    }

    public void setMethods( TestMethodReport[] methods )
    {
        this.methods = methods;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public void setTests( int tests )
    {
        this.tests = tests;
    }

    public void setTime( double time )
    {
        this.time = time;
    }

}
