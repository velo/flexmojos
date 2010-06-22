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
package org.eclipsecon.swtbot.example;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( SWTBotJunit4ClassRunner.class )
public class MyFirstTest
{

    private static SWTWorkbenchBot bot;

    @BeforeClass
    public static void beforeClass()
        throws Exception
    {
        bot = new SWTWorkbenchBot();
        bot.viewByTitle( "Welcome" ).close();
    }

    @Test
    public void canCreateANewJavaProject()
        throws Exception
    {
        bot.menu( "File" ).menu( "New" ).menu( "Project..." ).click();

        SWTBotShell shell = bot.shell( "New Project" );
        shell.activate();
        bot.tree().select( "Java Project" );
        bot.button( "Next >" ).click();

        bot.textWithLabel( "Project name:" ).setText( "MyFirstProject" );

        bot.button( "Finish" ).click();
        // FIXME: assert that the project is actually created, for later
    }

    @AfterClass
    public static void sleep()
    {
        bot.sleep( 2000 );
    }

}
