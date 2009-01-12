/*	
	
	Copyright (c) 2007-2008 Ryan Christiansen
	
	This software is provided 'as-is', without any express or implied
	warranty. In no event will the authors be held liable for any damages
	arising from the use of this software.
	
	Permission is granted to anyone to use this software for any purpose,
	including commercial applications, and to alter it and redistribute it
	freely, subject to the following restrictions:
	
	1. The origin of this software must not be misrepresented; you must not
	claim that you wrote the original software. If you use this software
	in a product, an acknowledgment in the product documentation would be
	appreciated but is not required.
	
	2. Altered source versions must be plainly marked as such, and must not be
	misrepresented as being the original software.
	
	3. This notice may not be removed or altered from any source
	distribution.
	
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
