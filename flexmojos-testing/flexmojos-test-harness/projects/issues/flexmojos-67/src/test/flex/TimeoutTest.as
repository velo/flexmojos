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