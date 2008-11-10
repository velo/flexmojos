package {
 import advancedflex.debugger.aut.framework.TestCase;
 import advancedflex.debugger.aut.framework.ns.*;

 public class TestCaseSample2 extends TestCase {
  //test only.
  test function cc():void {
   console.print("aaa");
  }
  //test and check time if it is timeout.
  time function dd():int {
   console.print("bbb");
   return 10;
  }
 }
}