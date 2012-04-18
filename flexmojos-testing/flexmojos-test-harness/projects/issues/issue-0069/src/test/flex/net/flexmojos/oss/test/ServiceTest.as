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
package net.flexmojos.oss.test {

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
