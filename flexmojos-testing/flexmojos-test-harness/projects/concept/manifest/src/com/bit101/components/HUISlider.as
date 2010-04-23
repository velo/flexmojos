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

	public class HUISlider extends UISlider
	{
		
		/**
		 * Constructor
		 * @param parent The parent DisplayObjectContainer on which to add this HUISlider.
		 * @param xpos The x position to place this component.
		 * @param ypos The y position to place this component.
		 * @param label The string to use as the label for this component.
		 * @param defaultHandler The event handling function to handle the default event for this component.
		 */
		public function HUISlider(parent:DisplayObjectContainer = null, x:Number = 0, y:Number = 0, label:String = "", defaultEventHandler:Function = null)
		{
			_sliderClass = HSlider;
			super(parent, x, y, label, defaultEventHandler);
		}
		
		/**
		 * Initializes the component.
		 */
		override protected function init():void
		{
			super.init();
			setSize(200, 18);
		}
		
		/**
		 * Centers the label when label text is changed.
		 */
		override protected function positionLabel():void
		{
			_valueLabel.x = _slider.x + _slider.width + 5;
		}
		
		
		
		
		///////////////////////////////////
		// public methods
		///////////////////////////////////
		
		/**
		 * Draws the visual ui of this component.
		 */
		override public function draw():void
		{
			super.draw();
			_slider.x = _label.width + 5;
			_slider.y = height / 2 - _slider.height / 2;
			_slider.width = width - _label.width - 50 - 10;
			
			_valueLabel.x = _slider.x + _slider.width + 5;
		}
		
		///////////////////////////////////
		// event handlers
		///////////////////////////////////
		
		///////////////////////////////////
		// getter/setters
		///////////////////////////////////
		
	}
}