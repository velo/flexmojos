/**
 *
 */
package org.sonatype.flexmojos.unitestingsupport.flexunit4
{
	import flex.lang.reflect.Klass;
	
	import org.flexunit.runner.Descriptor;
	import org.flexunit.runner.FlexUnitCore;
	import org.flexunit.runner.IDescription;
	import org.flexunit.runner.Result;
	import org.flexunit.runner.notification.Failure;
	import org.flexunit.runner.notification.IRunListener;
	import org.flexunit.runners.model.TestClass;

    import org.sonatype.flexmojos.unitestingsupport.ITestApplication;
	import org.sonatype.flexmojos.test.report.ErrorReport;
	import org.sonatype.flexmojos.unitestingsupport.SocketReporter;
	import org.sonatype.flexmojos.unitestingsupport.UnitTestRunner;

	public class FlexUnit4Listener implements IRunListener, UnitTestRunner
	{
		private var running:Boolean = false;

		private var _socketReporter:SocketReporter;
		
		public function FlexUnit4Listener(socketReporter:SocketReporter=null) 
		{
			this._socketReporter = socketReporter;
		}
		
		public function set socketReporter(socketReporter:SocketReporter):void {
			 this._socketReporter = socketReporter;
		}

        public function run( testApp:ITestApplication ):int
        {
            var tests:Array = testApp.tests;

			var listener:FlexUnit4Listener = new FlexUnit4Listener(_socketReporter);
			var flexUnitCore:FlexUnitCore = new FlexUnitCore();
			
 			flexUnitCore.addListener( listener );

			//This run statements executes the unit tests for the FlexUnit4 framework
 			var result:Result = flexUnitCore.run.apply( flexUnitCore, tests ); // The result seems to be always null

			var count:int = 0;
			for each (var test:Class in tests)
			{
				count += countTestCases(test);
			}
			return count;
		}
		
		private static function countTestCases(test:Class):int
		{
			var klassInfo:Klass = new Klass(test);

			if (klassInfo.hasMetaData("Suite"))
			{
				var suiteClasses:Array = getSuiteClasses(klassInfo);
				var count:int = 0;
				for each (var oneTest:Class in suiteClasses)
				{
					count += countTestCases(oneTest);
				}
				return count;
			}
			else
			{ // It's a Test ?
				var testMethods:Array = computeTestMethods(test);
				return testMethods.length;			
			}
		}
		
		/**
		 * Returns the methods that run tests. Default implementation 
		 * returns all methods annotated with {@code @Test} on this 
		 * class and superclasses that are not overridden.
		 */
		protected static function computeTestMethods(test:Class):Array
		{
			var testClass:TestClass = new TestClass(test);
			var testMethods:Array = testClass.getMetaDataMethods( "Test" );
			var ignoredMethods:Array = testClass.getMetaDataMethods( "Ignore" );

			var resultMethods:Array = new Array();
			for each (var method:* in testMethods)
			{
				if(ignoredMethods.indexOf(method) == -1)
				{
					resultMethods.push(method);
				}
			}
			
			return resultMethods;
		}

		/* This method comes from the FlexUnit4 org.flexunit.runners.Suite class */
		private static function getSuiteClasses( klassInfo:Klass ):Array
		{
			var classRef:Class;
			var classArray:Array = new Array();

			for ( var i:int=0; i<klassInfo.fields.length; i++ )
			{
				if ( !klassInfo.fields[ i ].isStatic )
				{
					try
					{
						classRef = klassInfo.fields[i].type;
						classArray.push(classRef); 
					}
					catch ( e:Error )
					{
						//Not sure who we should inform here yet. We will need someway of capturing the idea that this
						//is a missing class, but not sure where or how to promote that up the chain....if it is even possible
						//that we could have a missing class, given the way we are linking it
					}
				}
			}
			
			
			/***
			  <variable name="two" type="suite.cases::TestTwo"/>
			  <variable name="one" type="suite.cases::TestOne"/>

  			SuiteClasses annotation= klass.getAnnotation(SuiteClasses.class);
			if (annotation == null)
				throw new InitializationError(String.format("class '%s' must have a SuiteClasses annotation", klass.getName()));
			return annotation.value();
			 **/
			 //this needs to return the suiteclasses
			 return classArray;
		}

		public function testRunStarted( description:IDescription ):void
		{
			running = true;
		}
		
		public function testRunFinished( result:Result ):void
		{
			running = false;
		}
		
    	/**
    	 * Called when a Test starts.
    	 */
		public function testStarted( description:IDescription ):void
		{
			var descriptor:Descriptor = getDescriptorFromDescription(description);
			_socketReporter.addMethod( descriptor.path + "." + descriptor.suite, descriptor.method );
			trace("FlexUnit4: Test " + descriptor.method + " in " + descriptor.path + "." + descriptor.suite + " started");
		}
		
		/**
		 * Called when a Test ends.
		 */
		public function testFinished( description:IDescription ):void
		{
			var descriptor:Descriptor = getDescriptorFromDescription(description);
			_socketReporter.testFinished(descriptor.path + "." + descriptor.suite);
			trace("FlexUnit4: Test " + descriptor.method + " in " + descriptor.path + "." + descriptor.suite + " finished");
		}
		
		/**
		 * Called when a Failure occurs.
		 */
		public function testFailure( failure:Failure ):void
		{
			var descriptor:Descriptor = getDescriptorFromDescription(failure.description);
			var errorReport:ErrorReport = new ErrorReport();
			errorReport.type = descriptor.path + "." + descriptor.suite;
			errorReport.message = failure.exception.message;
			errorReport.stackTrace = failure.exception.getStackTrace();

			_socketReporter.addFailure(descriptor.path + "." + descriptor.suite, descriptor.method, errorReport);
			trace("FlexUnit4: Test " + descriptor.method + " in " + descriptor.path + "." + descriptor.suite + " failed");
		}

		/**
		 * Called when an Assumption Failure occurs.
		 */
		public function testAssumptionFailure( failure:Failure ):void
		{
			var descriptor:Descriptor = getDescriptorFromDescription(failure.description);
			var errorReport:ErrorReport = new ErrorReport();
			errorReport.type = descriptor.path + "." + descriptor.suite;
			errorReport.message = failure.exception.message;
			errorReport.stackTrace = failure.exception.getStackTrace();

			_socketReporter.addFailure(descriptor.path + "." + descriptor.suite, descriptor.method, errorReport);
			trace("FlexUnit4: Assumption Failure on Test " + descriptor.method + " in " + descriptor.path + "." + descriptor.suite);
		}
		
		public function testIgnored( description:IDescription ):void
		{
			var descriptor:Descriptor = getDescriptorFromDescription(description);
			trace("FlexUnit4: Test " + descriptor.method + " in " + descriptor.path + "." + descriptor.suite + " ignored");
		}
		
		/* This method comes from the FlexUnit4UIRunner org.flexunit.flexui.data.TestRunnerBasePresentationModel class */
 	    private function getDescriptorFromDescription(description:IDescription):Descriptor
 	    {
			var descriptor:Descriptor = new Descriptor();
			var descriptionArray:Array = description.displayName.split("::");
			descriptor.path = descriptionArray[0];
			
			//This code was assuming things would be in a package, which is a good call, but it crashed
			//badly on anything in the default package
			//It also assumes that every test would be in a suite. Also not a valid assumption
			var classMethod:String =  descriptionArray[1];
			var classMethodArray:Array;
			if (classMethod)
			{
				classMethodArray = classMethod.split(".");
			}
			else 
			{
				classMethod =  descriptionArray[0];
				classMethodArray = classMethod.split(".");
			}

			descriptor.suite = classMethodArray[0];
			descriptor.method = classMethodArray[1];

			return descriptor;
		}
	}
}
