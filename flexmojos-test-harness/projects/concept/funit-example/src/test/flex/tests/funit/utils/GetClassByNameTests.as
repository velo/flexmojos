/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package tests.funit.utils
{
	import funit.framework.*;
	
	import mx.core.Application;
	
	import sv.utils.ClassUtil;
	
	[TestFixture]
	public class GetClassByNameTests
	{
		
		
		public function GetClassByNameTests()
		{
			
		}
		
		
//------------------------------------------------------------------------------
		
		[Test]
		public function getClassByName() : void
		{
			Assert.areSame( Application, ClassUtil.getClassByName("mx.core.Application") );
		}
		
//------------------------------------------------------------------------------
		
		[Test]
		public function getClassByQualifiedName() : void
		{
			Assert.areSame( Application, ClassUtil.getClassByName("mx.core::Application") );
		}
		
//------------------------------------------------------------------------------
		
		[Test]
		[ExpectedError("sv.reflection.errors.ReflectionError")]
		public function getClassFailureOnSimpleName() : void
		{
			ClassUtil.getClassByName("Application");
		}
		
//------------------------------------------------------------------------------
		
		[Test]
		[ExpectedError("sv.reflection.errors.ReflectionError")]
		public function getClassFailureOnMalformedName() : void
		{
			ClassUtil.getClassByName("mx.core:Application");
		}
		
//------------------------------------------------------------------------------
		
		[Test]
		[ExpectedError("ArgumentError")]
		public function getClassFailureOnNull() : void
		{
			ClassUtil.getClassByName(null);
		}
		
//------------------------------------------------------------------------------
		
	}
	
}