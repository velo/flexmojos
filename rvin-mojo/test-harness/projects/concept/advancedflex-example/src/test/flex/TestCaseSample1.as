package {
 import advancedflex.debugger.aut.framework.Assert;
 import advancedflex.debugger.aut.framework.TestCase;
 import advancedflex.debugger.aut.framework.ns.*;

 public class TestCaseSample1 extends TestCase {
  //test only.
  test function a():void {
   console.print("aa");
  }
  //test and check time if it is timeout.
  time function b():int {
   console.print("bb");
   //if used time > return value,it will throw error.
   return 10;
  }
 }
}