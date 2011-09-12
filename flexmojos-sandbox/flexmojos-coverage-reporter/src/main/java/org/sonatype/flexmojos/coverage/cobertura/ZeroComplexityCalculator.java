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
package org.sonatype.flexmojos.coverage.cobertura;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.PackageData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.SourceFileData;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.util.FileFinder;

public class ZeroComplexityCalculator extends ComplexityCalculator {
	public ZeroComplexityCalculator( FileFinder finder ) {
		super( finder );
	}
	
	@Override
	public double getCCNForClass(ClassData classData) {
		return 0;
	}
	
	@Override
	public double getCCNForPackage(PackageData packageData) {
		return 0;
	}
	
	@Override
	public double getCCNForProject(ProjectData projectData) {
		return 0;
	}
	
	@Override
	public double getCCNForSourceFile(SourceFileData sourceFile) {
		return 0;
	}
}
