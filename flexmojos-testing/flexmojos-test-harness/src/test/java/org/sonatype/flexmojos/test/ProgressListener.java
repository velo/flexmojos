/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sonatype.flexmojos.test;

import java.io.PrintStream;

import org.apache.commons.io.output.NullOutputStream;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

public class ProgressListener
    extends TestListenerAdapter
{

    private PrintStream out;

    private PrintStream err;

    @Override
    public void onStart( ITestContext testContext )
    {
        super.onStart( testContext );

        out = System.out;
        err = System.err;
        System.setOut( new PrintStream( new NullOutputStream() ) );
        System.setErr( new PrintStream( new NullOutputStream() ) );
    }

    @Override
    public void onFinish( ITestContext testContext )
    {
        super.onFinish( testContext );

        System.setOut( out );
        System.setErr( err );
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage( ITestResult tr )
    {
        super.onTestFailedButWithinSuccessPercentage( tr );

        showResult( tr, "partial success", err );
    }

    @Override
    public void onTestFailure( ITestResult tr )
    {
        super.onTestFailure( tr );

        showResult( tr, "failed", err );
    }

    @Override
    public void onTestSkipped( ITestResult tr )
    {
        super.onTestSkipped( tr );

        showResult( tr, "skipped", err );
    }

    @Override
    public void onTestSuccess( ITestResult tr )
    {
        super.onTestSuccess( tr );

        showResult( tr, "success", out );
    }

    private void showResult( ITestResult result, String status, PrintStream printer )
    {
        printer.println( "Result: " + result.getTestClass().getName() + "." + result.getName() + "() ===> " + status );
    }

}
