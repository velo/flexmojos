package unittest {
    import org.flexunit.Assert;

    [RunWith("org.flexunit.runners.Parameterized")]
    public class ParametrizedTestWithFailuresTest {

        public static function providesData():Array {
            var values:Array = new Array(100);
            for (var i:int = 0; i < values.length; i++) {
                values[i] = ["SomeData-" + i];
            }
            return values;
        }

        [Test(dataProvider="providesData")]
        public function failingTest(testData:String):void {
            Assert.assertFalse(testData == "SomeData-98");
        }
    }
}
