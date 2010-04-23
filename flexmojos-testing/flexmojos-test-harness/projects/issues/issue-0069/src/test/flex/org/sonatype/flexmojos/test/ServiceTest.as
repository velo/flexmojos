/**
 *   Copyright 2008 Marvin Herman Froeder
 * -->
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.test {

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
