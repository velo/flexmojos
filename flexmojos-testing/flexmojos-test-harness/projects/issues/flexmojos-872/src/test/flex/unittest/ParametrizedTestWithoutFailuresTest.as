package unittest {
    import org.flexunit.Assert;

    [RunWith("org.flexunit.runners.Parameterized")]
    public class ParametrizedTestWithoutFailuresTest {

        public static function providesData():Array {
            var values:Array = new Array(100);
            for (var i:int = 0; i < values.length; i++) {
                values[i] = ["AnotherTestData-" + i];
            }
            return values;
        }

        [Test(dataProvider="providesData")]
        public function notFailingTest(testData:String):void {
        }

    }
}
