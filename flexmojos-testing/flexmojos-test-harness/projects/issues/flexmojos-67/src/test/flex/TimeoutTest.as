/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package 
{
    import flash.utils.Timer;
    import flash.events.TimerEvent;
	import flexunit.framework.TestCase;
	
	public class TimeoutTest extends TestCase
	{

		public function test1():void
		{
		    var timer:Timer = new Timer(5000, 1);
		    timer.addEventListener
		    (
		        TimerEvent.TIMER_COMPLETE,
		        addAsync
		        (
		            function(event:TimerEvent):void
		            {
		                trace("done test1");
		            },
		            10000
		        )
		    );
		    timer.start();
		}

		public function test2():void
		{
		    var timer:Timer = new Timer(5000, 1);
		    timer.addEventListener
		    (
		        TimerEvent.TIMER_COMPLETE,
		        addAsync
		        (
		            function(event:TimerEvent):void
		            {
		                trace("done test2");
		            },
		            10000
		        )
		    );
		    timer.start();
		}

		public function test3():void
		{
		    var timer:Timer = new Timer(5000, 1);
		    timer.addEventListener
		    (
		        TimerEvent.TIMER_COMPLETE,
		        addAsync
		        (
		            function(event:TimerEvent):void
		            {
		                trace("done test3");
		            },
		            10000
		        )
		    );
		    timer.start();
		}

		public function test4():void
		{
		    var timer:Timer = new Timer(5000, 1);
		    timer.addEventListener
		    (
		        TimerEvent.TIMER_COMPLETE,
		        addAsync
		        (
		            function(event:TimerEvent):void
		            {
		                trace("done test4");
		            },
		            10000
		        )
		    );
		    timer.start();
		}

	}
}