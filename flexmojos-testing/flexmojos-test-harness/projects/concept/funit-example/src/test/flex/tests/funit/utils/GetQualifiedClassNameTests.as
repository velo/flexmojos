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
	public class GetQualifiedClassNameTests
	{
		
		
		public function GetQualifiedClassNameTests()
		{
			
		}
		
		
//------------------------------------------------------------------------------
		
		[Test]
		public function getNameByInstance() : void
		{
			Assert.areEqual( "mx.core::Application", ClassUtil.getQualifiedClassName(new Application()) );
		}
		
//------------------------------------------------------------------------------
		
		[Test]
		public function getNameByClass() : void
		{
			Assert.areEqual( "mx.core::Application", ClassUtil.getQualifiedClassName(Application));
		}

//------------------------------------------------------------------------------
		
		[Test]
		public function getNameByBaseInstance() : void
		{
			Assert.areEqual( "Object", ClassUtil.getQualifiedClassName(new Object()) );
		}

//------------------------------------------------------------------------------
		
		[Test]
		public function getNameByBaseClass() : void
		{
			Assert.areEqual( "Object", ClassUtil.getQualifiedClassName(Object) );
		}
		
//------------------------------------------------------------------------------
		
		[Test]
		public function getNameByPrimitiveInstance() : void
		{
			Assert.areEqual( "Boolean", ClassUtil.getQualifiedClassName(true) );
		}
		
//------------------------------------------------------------------------------
		
		[Test]
		public function getNameByPrimitiveClass() : void
		{
			Assert.areEqual( "Boolean", ClassUtil.getQualifiedClassName(Boolean) );
		}
		
//------------------------------------------------------------------------------
		
		[Test]
		public function getNameHandleNull() : void
		{
			Assert.areEqual( "null", ClassUtil.getQualifiedClassName(null) );
		}
		
//------------------------------------------------------------------------------
		
		[Test]
		public function getNameHandleUndefined() : void
		{
			Assert.areEqual( "void", ClassUtil.getQualifiedClassName(undefined) );
		}
		
//------------------------------------------------------------------------------
		
		[Test]
		public function getNameHandleVoid() : void
		{
			Assert.areEqual( "void", ClassUtil.getQualifiedClassName(void) );
		}
		
//------------------------------------------------------------------------------
		
	}
	
}
