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
package net.flexmojos.oss.todolist.business {
	import flash.events.Event;
	import flash.events.IEventDispatcher;
	
	import mx.rpc.AsyncToken;
	import mx.rpc.remoting.RemoteObject;
	
	import net.flexmojos.oss.todolist.domain.TodoItem;
	

    public class TodoService implements ITodoService, IEventDispatcher {
    	
    	private var ro:RemoteObject;
    	
    	public function TodoService(destination:String) {
    		ro = new RemoteObject(destination);
    	}
    	
		public function addEventListener(type:String, listener:Function, useCapture:Boolean=false, priority:int=0, useWeakReference:Boolean=false):void
		{
			ro.addEventListener(type, listener, useCapture, priority, useWeakReference);
		}
		
		public function removeEventListener(type:String, listener:Function, useCapture:Boolean=false):void
		{
			ro.removeEventListener(type, listener, useCapture);
		}
		
		public function dispatchEvent(event:Event):Boolean
		{
			return ro.dispatchEvent(event);
		}
		
		public function hasEventListener(type:String):Boolean
		{
			return ro.hasEventListener(type);
		}
		
		public function willTrigger(type:String):Boolean
		{
			return ro.willTrigger(type);
		}
		
		public function remove(todoItem:TodoItem):AsyncToken
		{
			return ro.remove(todoItem);
		}
		
		public function save(todoItem:TodoItem):AsyncToken
		{
			return ro.save(todoItem);
		}
		
		public function findById(todoItem:TodoItem):AsyncToken
		{
			return ro.findById(todoItem);
		}
		
		public function getList():AsyncToken
		{
			return ro.getList();
		}
    	
    }
}