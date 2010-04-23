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
	import flash.display.Sprite;
	import flash.events.MouseEvent;

	public class PushButton extends Component
	{
		private var _back:Sprite;
		private var _face:Sprite;
		private var _label:Label;
		private var _labelText:String = "";
		private var _over:Boolean = false;
		private var _down:Boolean = false;
		private var _selected:Boolean = false;
		private var _toggle:Boolean = false;
		
		/**
		 * Constructor
		 * @param parent The parent DisplayObjectContainer on which to add this PushButton.
		 * @param xpos The x position to place this component.
		 * @param ypos The y position to place this component.
		 * @param label The string to use for the initial label of this component.
 		 * @param defaultHandler The event handling function to handle the default event for this component (click in this case).
		 */
		public function PushButton(parent:DisplayObjectContainer = null, xpos:Number = 0, ypos:Number =  0, label:String = "", defaultHandler:Function = null)
		{
			super(parent, xpos, ypos);
			if(defaultHandler != null)
			{
				addEventListener(MouseEvent.CLICK, defaultHandler);
			}
			this.label = label;
		}
		
		/**
		 * Initializes the component.
		 */
		override protected function init():void
		{
			super.init();
			buttonMode = true;
			useHandCursor = true;
			setSize(100, 20);
		}
		
		/**
		 * Creates and adds the child display objects of this component.
		 */
		override protected function addChildren():void
		{
			_back = new Sprite();
			_back.filters = [getShadow(2, true)];
			_back.mouseEnabled = false;
			addChild(_back);
			
			_face = new Sprite();
			_face.mouseEnabled = false;
			_face.filters = [getShadow(1)];
			_face.x = 1;
			_face.y = 1;
			addChild(_face);
			
			_label = new Label();
			addChild(_label);
			
			addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
			addEventListener(MouseEvent.ROLL_OVER, onMouseOver);
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
			_back.graphics.clear();
			_back.graphics.beginFill(Style.BACKGROUND);
			_back.graphics.drawRect(0, 0, _width, _height);
			_back.graphics.endFill();
			
			_face.graphics.clear();
			_face.graphics.beginFill(Style.BUTTON_FACE);
			_face.graphics.drawRect(0, 0, _width - 2, _height - 2);
			_face.graphics.endFill();
			
			_label.autoSize = true;
			_label.text = _labelText;
			if(_label.width > _width - 4)
			{
				_label.autoSize = false;
				_label.width = _width - 4;
			}
			else
			{
				_label.autoSize = true;
			}
			_label.draw();
			_label.move(_width / 2 - _label.width / 2, _height / 2 - _label.height / 2);
			
		}
		
		
		
		
		///////////////////////////////////
		// event handlers
		///////////////////////////////////
		
		/**
		 * Internal mouseOver handler.
		 * @param event The MouseEvent passed by the system.
		 */
		protected function onMouseOver(event:MouseEvent):void
		{
			_over = true;
			addEventListener(MouseEvent.ROLL_OUT, onMouseOut);
		}
		
		/**
		 * Internal mouseOut handler.
		 * @param event The MouseEvent passed by the system.
		 */
		protected function onMouseOut(event:MouseEvent):void
		{
			_over = false;
			if(!_down)
			{
				_face.filters = [getShadow(1)];
			}
		}
		
		/**
		 * Internal mouseOut handler.
		 * @param event The MouseEvent passed by the system.
		 */
		protected function onMouseDown(event:MouseEvent):void
		{
			_down = true;
			_face.filters = [getShadow(1, true)];
			stage.addEventListener(MouseEvent.MOUSE_UP, onMouseUp);
		}
		
		/**
		 * Internal mouseUp handler.
		 * @param event The MouseEvent passed by the system.
		 */
		protected function onMouseUp(event:MouseEvent):void
		{
			if(_toggle  && _over)
			{
				_selected = !_selected;
			}
			_down = _selected;
			_face.filters = [getShadow(1, _selected)];
			stage.removeEventListener(MouseEvent.MOUSE_UP, onMouseUp);
		}
		
		
		
		
		///////////////////////////////////
		// getter/setters
		///////////////////////////////////
		
		/**
		 * Sets / gets the label text shown on this Pushbutton.
		 */
		public function set label(str:String):void
		{
			_labelText = str;
			draw();
		}
		public function get label():String
		{
			return _labelText;
		}
		
		public function set selected(value:Boolean):void
		{
			if(!_toggle)
			{
				value = false;
			}
			
			_selected = value;
			_down = _selected;
			_face.filters = [getShadow(1, _selected)];
		}
		public function get selected():Boolean
		{
			return _selected;
		}
		
		public function set toggle(value:Boolean):void
		{
			_toggle = value;
		}
		public function get toggle():Boolean
		{
			return _toggle;
		}
		
		
	}
}