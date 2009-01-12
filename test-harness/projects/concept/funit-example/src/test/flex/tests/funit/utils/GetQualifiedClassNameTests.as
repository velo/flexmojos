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
