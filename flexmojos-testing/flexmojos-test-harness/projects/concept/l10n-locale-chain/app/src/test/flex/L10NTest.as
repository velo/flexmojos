/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
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
/**
 * @author Seven
 */
package
{

	import flexunit.framework.Assert;

	import mx.resources.*;

	[ResourceBundle("text")]
	[ResourceBundle("collections")]
	public class L10NTest
	{

		[Test]
		public function addition():void
		{
			var rm:IResourceManager=ResourceManager.getInstance();
			rm.localeChain=['en_US'];
			Assert.assertEquals('Main View', rm.getString("text", "TITLE", null, "en_US"));
			Assert.assertEquals('Vista principal', rm.getString("text", "TITLE", null, "pt_PT"));

//noItems fall back from pt_PT to pt_BR
			Assert.assertEquals('No items to search.', rm.getString("collections", "noItems", null, "en_US"));
			Assert.assertEquals('Nenhum item a ser pesquisado.', rm.getString("collections", "noItems", null, "pt_PT"));
		}

	}

}