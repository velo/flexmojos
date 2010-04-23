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
	import flash.display.Stage;
	import flash.display.StageAlign;
	import flash.display.StageScaleMode;
	import flash.events.Event;
	import flash.filters.DropShadowFilter;

	public class Component extends Sprite
	{
		[Embed(source="/assets/pf_ronda_seven.ttf", fontName="PF Ronda Seven", mimeType="application/x-font")]
		private var Ronda:Class;
		
		protected var _width:Number = 0;
		protected var _height:Number = 0;
		
		public static const DRAW:String = "draw";

		/**
		 * Constructor
		 * @param parent The parent DisplayObjectContainer on which to add this component.
		 * @param xpos The x position to place this component.
		 * @param ypos The y position to place this component.
		 */
		public function Component(parent:DisplayObjectContainer = null, xpos:Number = 0, ypos:Number =  0)
		{
			move(xpos, ypos);
			if(parent != null)
			{
				parent.addChild(this);
			}
			init();
		}
		
		/**
		 * Initilizes the component.
		 */
		protected function init():void
		{
			addChildren();
			invalidate();
		}
		
		/**
		 * Overriden in subclasses to create child display objects.
		 */
		protected function addChildren():void
		{
			
		}
		
		/**
		 * DropShadowFilter factory method, used in many of the components.
		 * @param dist The distance of the shadow.
		 * @param knockout Whether or not to create a knocked out shadow.
		 */
		protected function getShadow(dist:Number, knockout:Boolean = false):DropShadowFilter
		{
			return new DropShadowFilter(dist, 45, Style.DROPSHADOW, 1, dist, dist, .3, 1, knockout);
		}
		
		/**
		 * Marks the component to be redrawn on the next frame.
		 */
		protected function invalidate():void
		{
//			draw();
			addEventListener(Event.ENTER_FRAME, onInvalidate);
		}
		
		
		
		
		///////////////////////////////////
		// public methods
		///////////////////////////////////
		
		/**
		 * Utility method to set up usual stage align and scaling.
		 */
		public static function initStage(stage:Stage):void
		{
			stage.align = StageAlign.TOP_LEFT;
			stage.scaleMode = StageScaleMode.NO_SCALE;
		}
		
		/**
		 * Moves the component to the specified position.
		 * @param xpos the x position to move the component
		 * @param ypos the y position to move the component
		 */
		public function move(xpos:Number, ypos:Number):void
		{
			x = Math.round(xpos);
			y = Math.round(ypos);
		}
		
		/**
		 * Sets the size of the component.
		 * @param w The width of the component.
		 * @param h The height of the component.
		 */
		public function setSize(w:Number, h:Number):void
		{
			_width = w;
			_height = h;
			invalidate();
		}
		
		/**
		 * Abstract draw function.
		 */
		public function draw():void
		{
			dispatchEvent(new Event(Component.DRAW));
		}
		
		
		
		
		///////////////////////////////////
		// event handlers
		///////////////////////////////////
		
		/**
		 * Called one frame after invalidate is called.
		 */
		protected function onInvalidate(event:Event):void
		{
			removeEventListener(Event.ENTER_FRAME, onInvalidate);
			draw();
		}
		
		
		
		
		///////////////////////////////////
		// getter/setters
		///////////////////////////////////
		
		/**
		 * Sets/gets the width of the component.
		 */
		override public function set width(w:Number):void
		{
			_width = w;
			invalidate();
			dispatchEvent(new Event(Event.RESIZE));
		}
		override public function get width():Number
		{
			return _width;
		}
		
		/**
		 * Sets/gets the height of the component.
		 */
		override public function set height(h:Number):void
		{
			_height = h;
			invalidate();
			dispatchEvent(new Event(Event.RESIZE));
		}
		override public function get height():Number
		{
			return _height;
		}
		
		/**
		 * Overrides the setter for x to always place the component on a whole pixel.
		 */
		override public function set x(value:Number):void
		{
			super.x = Math.round(value);
		}
		
		/**
		 * Overrides the setter for y to always place the component on a whole pixel.
		 */
		override public function set y(value:Number):void
		{
			super.y = Math.round(value);
		}
	}
}