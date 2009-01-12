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
	public class DescribeTypeTests
	{
		
		
		public function DescribeTypeTests()
		{
			
		}
		
		
//------------------------------------------------------------------------------
		
		[Test]
		public function describeTypeByClass() : void
		{
			Assert.isNotNull( ClassUtil.describeType(Application));
		}

//------------------------------------------------------------------------------
		
		[Test]
		public function describeTypeByBaseClass() : void
		{
			Assert.isNotNull( ClassUtil.describeType(Object) );
		}
		
//------------------------------------------------------------------------------
		
		[Test]
		public function describeTypePrimitiveClass() : void
		{
			Assert.isNotNull( ClassUtil.describeType(Boolean) );
		}
		
//------------------------------------------------------------------------------
		
		[Test]
		[ExpectedError("ArgumentError")]
		public function describeTypeFailureOnNull() : void
		{
			ClassUtil.describeType(null);
		}
		
//------------------------------------------------------------------------------
		
	}
	
}
