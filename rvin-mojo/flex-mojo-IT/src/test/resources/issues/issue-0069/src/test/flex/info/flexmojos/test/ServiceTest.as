package info.flexmojos.test {

	import flash.events.Event;
	import flash.events.TimerEvent;
	import flash.utils.Timer;

	import flexunit.framework.TestCase;

	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;

	public class ServiceTest extends TestCase {

		private var resultCheckTimer:Timer;

		private var isResultOK:Boolean;

		public function testService():void {
			var httpService:HTTPService = new HTTPService();
			httpService.url = "source.xml";
			httpService.addEventListener(ResultEvent.RESULT, onResultHttpService);
			httpService.addEventListener(FaultEvent.FAULT, onFaultHttpService);
			httpService.send();

			resultCheckTimer = new Timer(1);
			resultCheckTimer.delay = 1000;
			resultCheckTimer.addEventListener(TimerEvent.TIMER, addAsync(checkResult, 1500));
			resultCheckTimer.start();
		}

		private function onResultHttpService(e:ResultEvent):void {
			isResultOK = true;
		}

		private function onFaultHttpService(e:FaultEvent):void {
			fail(e.fault.toString());
			isResultOK = false;
		}

		private function checkResult(e:Event):void {
			assertTrue(isResultOK);
		}

	}
}
