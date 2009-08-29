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
	import flash.display.Sprite;
	import flash.events.MouseEvent;
	
	public class RadioButton extends Component
	{
		private var _back:Sprite;
		private var _button:Sprite;
		private var _selected:Boolean = false;
		private var _label:Label;
		private var _labelText:String = "";
		
		private static var buttons:Array;
		
		
		/**
		 * Constructor
		 * @param parent The parent DisplayObjectContainer on which to add this RadioButton.
		 * @param xpos The x position to place this component.
		 * @param ypos The y position to place this component.
		 * @param label The string to use for the initial label of this component.
		 * @param defaultHandler The event handling function to handle the default event for this component (click in this case).
		 */
		public function RadioButton(parent:DisplayObjectContainer = null, xpos:Number = 0, ypos:Number =  0, label:String = "", checked:Boolean = false, defaultHandler:Function = null)
		{
			RadioButton.addButton(this);
			_selected = checked;
			_labelText = label;
			super(parent, xpos, ypos);
			if(defaultHandler != null)
			{
				addEventListener(MouseEvent.CLICK, defaultHandler);
			}
		}
		
		/**
		 * Static method to add the newly created RadioButton to the list of buttons in the group.
		 * @param rb The RadioButton to add.
		 */
		private static function addButton(rb:RadioButton):void
		{
			if(buttons == null)
			{
				buttons = new Array();
			}
			buttons.push(rb);
		}
		
		/**
		 * Unselects all RadioButtons in the group, except the one passed.
		 * This could use some rethinking or better naming.
		 * @param rb The RadioButton to remain selected.
		 */
		private static function clear(rb:RadioButton):void
		{
			for(var i:uint = 0; i < buttons.length; i++)
			{
				if(buttons[i] != rb)
				{
					buttons[i].selected = false;
				}
			}
		}
		
		/**
		 * Initializes the component.
		 */
		override protected function init():void
		{
			super.init();
			
			buttonMode = true;
			useHandCursor = true;
			
			addEventListener(MouseEvent.CLICK, onClick, false, 1);
			selected = _selected;
		}
		
		/**
		 * Creates and adds the child display objects of this component.
		 */
		override protected function addChildren():void
		{
			_back = new Sprite();
			_back.filters = [getShadow(2, true)];
			addChild(_back);
			
			_button = new Sprite();
			_button.filters = [getShadow(1)];
			_button.visible = false;
			addChild(_button);
			
			_label = new Label(this, 0, 0, _labelText);
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
			_back.graphics.clear();
			_back.graphics.beginFill(Style.BACKGROUND);
			_back.graphics.drawCircle(5, 5, 5);
			_back.graphics.endFill();
			
			_button.graphics.clear();
			_button.graphics.beginFill(Style.BUTTON_FACE);
			_button.graphics.drawCircle(5, 5, 3);
			
			_label.x = 12;
			_label.y = (10 - _label.height) / 2;
			_label.text = _labelText;
			_label.draw();
			_width = _label.width + 12;
			_height = 10;
		}
		
		
		
		
		///////////////////////////////////
		// event handlers
		///////////////////////////////////
		
		/**
		 * Internal click handler.
		 * @param event The MouseEvent passed by the system.
		 */
		protected function onClick(event:MouseEvent):void
		{
			selected = true;
		}
		
		
		
		
		///////////////////////////////////
		// getter/setters
		///////////////////////////////////
		
		/**
		 * Sets / gets the selected state of this CheckBox.
		 */
		public function set selected(s:Boolean):void
		{
			_selected = s;
			_button.visible = _selected;
			if(_selected)
			{
				RadioButton.clear(this);
			}
		}
		public function get selected():Boolean
		{
			return _selected;
		}

		/**
		 * Sets / gets the label text shown on this CheckBox.
		 */
		public function set label(str:String):void
		{
			_labelText = str;
			invalidate();
		}
		public function get label():String
		{
			return _labelText;
		}
		
	}
}