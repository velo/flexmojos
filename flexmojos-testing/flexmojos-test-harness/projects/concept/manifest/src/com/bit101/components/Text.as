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