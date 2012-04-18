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
package net.flexmojos.oss.coverage.util;


public class ApparatUtil
{

    public static String toClassname( String apparatClassname)
    {
        // F:\4.x\flexmojos-aggregator\flexmojos-testing\flexmojos-test-harness\target\projects\concept\flexunit-example_testFlexUnitExample\src;com\adobe\example;Calculator.as
        String cn = apparatClassname;
        // com\adobe\example;Calculator.as
        cn = cn.substring( cn.indexOf( ';' ) + 1 );
        cn = cn.substring( 0, cn.indexOf( '.' ) );
        cn = cn.replace( '/', '.' ).replace( '\\', '.' ).replace( ';', '.' );
        return cn;
    }

}
