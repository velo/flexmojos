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
	import mx.core.LayoutContainer;
	
	import sv.utils.ClassUtil;
	
	[TestFixture]
	public class GetBaseClassTests
	{
		
		
		public function GetBaseClassTests()
		{
			
		}
		
		
//------------------------------------------------------------------------------
		
		[Test]
		public function getClassByInstance() : void
		{
			Assert.areSame( LayoutContainer, ClassUtil.getBaseClass(new Application()) );
		}
		
//------------------------------------------------------------------------------
		
		[Test]
		public function getClassByClass() : void
		{
			Assert.areSame( LayoutContainer, ClassUtil.getBaseClass(Application) );
		}
		
//------------------------------------------------------------------------------
		
		[Test]
		public function getClassByBaseInstance() : void
		{
			Assert.isNull( ClassUtil.getBaseClass(new Object()) );
		}
		
//------------------------------------------------------------------------------
		
		[Test]
		public function getClassByBaseClass() : void
		{
			Assert.isNull( ClassUtil.getBaseClass(Object) );
		}
		
//------------------------------------------------------------------------------		
		
		[Test]
		public function getClassByPrimitiveInstance() : void
		{
			Assert.areSame( Object, ClassUtil.getBaseClass(true) );
		}
		
//------------------------------------------------------------------------------
		
		[Test]
		public function getClassByPrimitiveClass() : void
		{
			Assert.areSame( Object, ClassUtil.getBaseClass(Boolean) );
		}
		
//------------------------------------------------------------------------------
		
		[Test]
		[ExpectedError("ArgumentError")]
		public function getClassFailureOnNull() : void
		{
			ClassUtil.getBaseClass(null);
		}
		
//------------------------------------------------------------------------------
		
	}
	
}
