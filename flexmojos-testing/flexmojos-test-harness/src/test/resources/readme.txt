====
    Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
    Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
====

The configurations under this folders are for integration tests only.  Shouldn't be used on production.

mvn org.sonatype.plugins:bundle-publisher-maven-plugin:1.2-SNAPSHOT:deploy-bundle -Dbundle=C:\flex\flex_sdk_4.0.0.13875\flex_sdk_4.0.0.13875.zip -Ddescriptor=compiler-descriptor.xml -Durl=http://repository.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=rso
mvn org.sonatype.plugins:bundle-publisher-maven-plugin:1.2-SNAPSHOT:deploy-bundle -Dbundle=C:\flex\flex_sdk_4.0.0.13875\flex_sdk_4.0.0.13875.zip -Ddescriptor=framework-descriptor.xml -Durl=http://repository.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=rso
