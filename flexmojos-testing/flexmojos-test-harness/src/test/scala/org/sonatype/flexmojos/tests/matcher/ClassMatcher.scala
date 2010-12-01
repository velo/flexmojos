package org.sonatype.flexmojos.tests.matcher
import apparat.abc._
import apparat.bytecode._
import apparat.swf._
import java.io.{
  File => JFile
}
import org.hamcrest.{ TypeSafeMatcher, Description, Matcher }

object ClassMatcher {
  def hasClass(classname: String): Matcher[JFile] = {
    return new ClassMatcher(classname);
  }
}
class ClassMatcher(classnames: String*) extends TypeSafeMatcher[JFile] {

  private var fileTested: JFile = null;
  private var found = new StringBuilder();

  def matchesSafely(file: JFile): Boolean = {
    fileTested = file;
    for {
      tag <- Swf fromFile file
      abc <- Abc fromTag tag
      nominal <- abc.types
    } {
      var foundName = nominal.name.namespace.name.name + ":" + nominal.name.name.name;
      if (classnames contains foundName) {
        return true;
      } else {
        found append foundName
        found append ", "
      }
    }
    false;
  }

  def describeTo(desc: Description): Unit = {
    desc.appendText(" contains ");
    for (classname <- classnames) {
      desc.appendValue(classname);
      desc.appendText(", ");
    }
    desc.appendValue(" instead found ");
    desc.appendText(found.toString)
  }

}