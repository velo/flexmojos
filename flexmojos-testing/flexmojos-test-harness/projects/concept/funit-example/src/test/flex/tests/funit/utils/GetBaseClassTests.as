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
