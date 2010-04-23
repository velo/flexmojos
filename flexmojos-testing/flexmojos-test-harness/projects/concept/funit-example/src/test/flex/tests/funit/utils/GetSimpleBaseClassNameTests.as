/**
 *   Copyright 2008 Marvin Herman Froeder
 * -->
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tests.funit.utils
{
	import funit.framework.*;
	
	import mx.core.Application;
	
	import sv.utils.ClassUtil;
	
	[TestFixture]
	public class GetSimpleBaseClassNameTests
	{
		
		
		public function GetSimpleBaseClassNameTests()
		{
			
		}
		
		
//------------------------------------------------------------------------------
		
		[Test]
		public function getNameByInstance() : void
		{
			Assert.areEqual( "LayoutContainer", ClassUtil.getSimpleBaseClassName(new Application()) );
		}
		
//------------------------------------------------------------------------------
		
		[Test]
		public function getNameByClass() : void
		{
			Assert.areEqual( "LayoutContainer", ClassUtil.getSimpleBaseClassName(Application));
		}

//------------------------------------------------------------------------------
		
		[Test]
		public function getNameByBaseInstance() : void
		{
			Assert.isNull( ClassUtil.getSimpleBaseClassName(new Object()) );
		}

//------------------------------------------------------------------------------
		
		[Test]
		public function getNameByBaseClass() : void
		{
			Assert.isNull( ClassUtil.getSimpleBaseClassName(Object) );
		}
		
//------------------------------------------------------------------------------
		
		[Test]
		public function getNameByPrimitiveInstance() : void
		{
			Assert.areEqual( "Object", ClassUtil.getSimpleBaseClassName(true) );
		}
		
//------------------------------------------------------------------------------
		
		[Test]
		public function getNameByPrimitiveClass() : void
		{
			Assert.areEqual( "Object", ClassUtil.getSimpleBaseClassName(Boolean) );
		}
		
//------------------------------------------------------------------------------
		
		[Test]
		[ExpectedError("ArgumentError")]
		public function getNameFailureOnNull() : void
		{
			ClassUtil.getSimpleBaseClassName(null);
		}
		
//------------------------------------------------------------------------------
		
	}
	
}
