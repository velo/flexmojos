/**
 * Created by christoferdutz on 23.10.14.
 */
package {
import flexunit.framework.Assert;

public class TestClassTest {

    [Test]
    public function testSomething():void {
        var obj:TestClass = new TestClass();
        Assert.assertEquals(obj.getLabel(), "Test Label");
    }

}
}
