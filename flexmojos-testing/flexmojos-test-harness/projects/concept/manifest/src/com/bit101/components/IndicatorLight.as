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
	import flash.display.GradientType;
	import flash.display.Shape;
	import flash.events.TimerEvent;
	import flash.geom.Matrix;
	import flash.utils.Timer;
	
	public class IndicatorLight extends Component
	{
		private var _color:uint;
		private var _lit:Boolean = false;
		private var _label:Label;
		private var _labelText:String = "";
		private var _lite:Shape;
		private var _timer:Timer;
		
		
		
		/**
		 * Constructor
		 * @param parent The parent DisplayObjectContainer on which to add this CheckBox.
		 * @param xpos The x position to place this component.
		 * @param ypos The y position to place this component.
		 * @param color The color of this light.
		 * @param label String containing the label for this component.
		 */
		public function IndicatorLight(parent:DisplayObjectContainer = null, xpos:Number = 0, ypos:Number =  0, color:uint = 0xff0000, label:String = "")
		{
			_color = color;
			_labelText = label;
			super(parent, xpos, ypos);
		}

		/**
		 * Initializes the component.
		 */
		override protected function init():void
		{
			super.init();
			_timer = new Timer(500);
			_timer.addEventListener(TimerEvent.TIMER, onTimer);
		}
		
		/**
		 * Creates the children for this component
		 */
		override protected function addChildren():void
		{
			_lite = new Shape();
			addChild(_lite);
			
			_label = new Label(this, 0, 0, _labelText);
			draw();
		}
		
		/**
		 * Draw the light.
		 */
		protected function drawLite():void
		{
			var colors:Array;
			if(_lit)
			{
				colors = [0xffffff, _color];
			}
			else
			{
				colors = [0xffffff, 0];
			}
			
			_lite.graphics.clear();
			var matrix:Matrix = new Matrix();
			matrix.createGradientBox(10, 10, 0, -2.5, -2.5);
			_lite.graphics.beginGradientFill(GradientType.RADIAL, colors, [1, 1], [0, 255], matrix);
			_lite.graphics.drawCircle(5, 5, 5);
			_lite.graphics.endFill();
		}
		
		
		
		///////////////////////////////////
		// event handler
		///////////////////////////////////
		
		/**
		 * Internal timer handler.
		 * @param event The TimerEvent passed by the system.
		 */
		protected function onTimer(event:TimerEvent):void
		{
			_lit = !_lit;
			draw();
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
			drawLite();
			
			_label.text = _labelText;
			_label.x = 12;
			_label.y = (10 - _label.height) / 2;
			_width = _label.width + 12;
			_height = 10;
		}
		
		/**
		 * Causes the light to flash on and off at the specified interval (milliseconds). A value less than 1 stops the flashing.
		 */
		public function flash(interval:int = 500):void
		{
			if(interval < 1)
			{
				_timer.stop();
				isLit = false;
				return;
			}
			_timer.delay = interval;
			_timer.start();
		}
		
		
		
		
		///////////////////////////////////
		// getter/setters
		///////////////////////////////////
		
		/**
		 * Sets or gets whether or not the light is lit.
		 */
		public function set isLit(value:Boolean):void
		{
			_timer.stop();
			_lit = value;
			drawLite();
		}
		public function get isLit():Boolean
		{
			return _lit;
		}
		
		/**
		 * Sets / gets the color of this light (when lit).
		 */
		public function set color(value:uint):void
		{
			_color = value;
			draw();
		}
		public function get color():uint
		{
			return _color;
		}
		
		/**
		 * Returns whether or not the light is currently flashing.
		 */
		public function get isFlashing():Boolean
		{
			return _timer.running;
		}
		
		/**
		 * Sets / gets the label text shown on this component.
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
		

	}
}