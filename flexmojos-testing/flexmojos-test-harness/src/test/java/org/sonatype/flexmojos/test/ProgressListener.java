/**
 * Copyright 2008 Marvin Herman Froeder
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
