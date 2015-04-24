/**
 * Flexmojos is a set of maven goals to allow maven users to compile,
 * optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
