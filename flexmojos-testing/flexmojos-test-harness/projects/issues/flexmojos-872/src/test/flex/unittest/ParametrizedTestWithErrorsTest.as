package unittest {
    import org.flexunit.Assert;

    [RunWith("org.flexunit.runners.Parameterized")]
    public class ParametrizedTestWithErrorsTest {

        public static function providesData():Array {
            var values:Array = new Array(100);
            for (var i:int = 0; i < values.length; i++) {
                values[i] = ["SomeData-" + i];
            }
            return values;
        }

        [Test(dataProvider="providesData")]
        public function failingTest(testData:String):void {
            if(testData == "SomeData-98") {
                throw new Error("Failure");
            }
        }
    }
}
