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
package net.flexmojos.oss.tests.matcher
import apparat.abc._
import apparat.swf._
import java.io.{
  File => JFile
}
import org.hamcrest.{ TypeSafeMatcher, Description, Matcher }

object ClassMatcher {
  def hasClass(classname: String): Matcher[JFile] = {
    /*return*/ new ClassMatcher(classname)
  }
}
class ClassMatcher(classnames: String*) extends TypeSafeMatcher[JFile] {

  private var fileTested: JFile = null
  private val found = new StringBuilder()

  def matchesSafely(file: JFile): Boolean = {
    fileTested = file
    for {
      tag <- Swf fromFile file
      abc <- Abc fromTag tag
      nominal <- abc.types
    } {
      val foundName = nominal.name.namespace.name.name + ":" + nominal.name.name.name
      if (classnames contains foundName) {
        return true
      } else {
        found append foundName
        found append ", "
      }
    }
    false
  }

  def describeTo(desc: Description): Unit = {
    desc.appendText(" contains ")
    for (classname <- classnames) {
      desc.appendValue(classname)
      desc.appendText(", ")
    }
    desc.appendValue(" instead found ")
    desc.appendText(found.toString())
  }

}