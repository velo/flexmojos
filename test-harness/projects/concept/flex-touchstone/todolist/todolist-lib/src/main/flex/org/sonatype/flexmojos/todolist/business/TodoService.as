/**
 * Copyright 2008 Marvin Herman Froeder
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sonatype.flexmojos.todolist.business {
	import flash.events.Event;
	import flash.events.IEventDispatcher;
	
	import mx.rpc.AsyncToken;
	import mx.rpc.remoting.RemoteObject;
	
	import org.sonatype.flexmojos.todolist.domain.TodoItem;
	

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