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
package net.flexmojos.oss.plugin.common;

import net.flexmojos.oss.plugin.AbstractMavenMojo;
import net.flexmojos.oss.plugin.compiler.AbstractFlexCompilerMojo;

public aspect QuickMode
{
    private static final String FLEXMOJOS_QUICK_MODE_ACTIVE = "Flexmojos-quick-mode-active";

    pointcut compilerSkipper() :  (target(AbstractFlexCompilerMojo) )&&
        execution(void execute() ) ;

    pointcut genericSkipper() :  (target (AbstractMavenMojo) && !target(AbstractFlexCompilerMojo) )&&
    execution(void execute() ) ;

    void around() : genericSkipper() {
        AbstractMavenMojo mojo = (AbstractMavenMojo) thisJoinPoint.getThis();
        if ( mojo.getPluginContext().containsKey( FLEXMOJOS_QUICK_MODE_ACTIVE ) )
        {
            mojo.getLog().info( "Quick mode kick in, no need to recompile the project." );
            return;
        }

        proceed();
    }

    void around() : compilerSkipper() {
        AbstractFlexCompilerMojo<?, ?> mojo = (AbstractFlexCompilerMojo<?, ?>) thisJoinPoint.getThis();
        if ( !mojo.isCompilationRequired() )
        {
            mojo.getLog().info( "Quick mode kick in, no need to recompile the project." );
            mojo.getPluginContext().put( FLEXMOJOS_QUICK_MODE_ACTIVE, true );
            return;
        }

        proceed();
    }
}
