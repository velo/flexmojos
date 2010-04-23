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
package com.bit101.components
{
	import flash.display.DisplayObjectContainer;
	import flash.events.Event;
	import flash.text.TextField;
	import flash.text.TextFieldType;
	import flash.text.TextFormat;
	
	public class Text extends Component
	{
		private var _tf:TextField;
		private var _text:String = "";
		private var _editable:Boolean = true;
		private var _panel:Panel;
		
		
		/**
		 * Constructor
		 * @param parent The parent DisplayObjectContainer on which to add this Label.
		 * @param xpos The x position to place this component.
		 * @param ypos The y position to place this component.
		 * @param text The initial text to display in this component.
		 */
		public function Text(parent:DisplayObjectContainer = null, xpos:Number = 0, ypos:Number =  0, text:String = "")
		{
			_text = text;
			super(parent, xpos, ypos);
			setSize(200, 100);
		}
		
		/**
		 * Initializes the component.
		 */
		override protected function init():void
		{
			super.init();
		}
		
		/**
		 * Creates and adds the child display objects of this component.
		 */
		override protected function addChildren():void
		{
			_panel = new Panel(this);
			_panel.color = 0xffffff;
			
			_tf = new TextField();
			_tf.x = 2;
			_tf.y = 2;
			_tf.height = _height;
			_tf.embedFonts = true;
			_tf.multiline = true;
			_tf.wordWrap = true;
			_tf.selectable = true;
			_tf.type = TextFieldType.INPUT;
			_tf.defaultTextFormat = new TextFormat("PF Ronda Seven", 8, Style.LABEL_TEXT);
			_tf.addEventListener(Event.CHANGE, onChange);			
			addChild(_tf);
		}
		
		
		
		
		///////////////////////////////////
		// public methods
		///////////////////////////////////
		
		/**
		 * Draws the visual ui of the component.
		 */
		override public function draw():void
		{
			super.draw();
			
			_panel.setSize(_width, _height);
			
			_tf.width = _width - 4;
			_tf.height = _height - 4;
			_tf.text = _text;
			if(_editable)
			{
				_tf.mouseEnabled = true;
				_tf.selectable = true;
				_tf.type = TextFieldType.INPUT;
			}
			else
			{
				_tf.mouseEnabled = false;
				_tf.selectable = false;
				_tf.type = TextFieldType.DYNAMIC;
			}
		}
		
		
		
		
		///////////////////////////////////
		// event handlers
		///////////////////////////////////
		
		protected function onChange(event:Event):void
		{
			_text = _tf.text;
			dispatchEvent(event);
		}
		
		///////////////////////////////////
		// getter/setters
		///////////////////////////////////
		
		/**
		 * Gets / sets the text of this Label.
		 */
		public function set text(t:String):void
		{
			_text = t;
			invalidate();
		}
		public function get text():String
		{
			return _text;
		}
		
		/**
		 * Gets / sets whether or not this text component will be editable.
		 */
		public function set editable(b:Boolean):void
		{
			_editable = b;
			invalidate();
		}
		public function get editable():Boolean
		{
			return _editable;
		}
	}
}