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