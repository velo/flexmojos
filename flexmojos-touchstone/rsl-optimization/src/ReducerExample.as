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
	import flash.display.Graphics;
	import flash.display.Shape;
	import flash.display.Sprite;
	import flash.display.StageQuality;
	import flash.display.StageScaleMode;
	import flash.events.Event;
	import flash.geom.Rectangle;

	[SWF(width='800',height='600',frameRate='32',backgroundColor='0x000000')]
	
	/**
	 * @author Joa Ebert
	 */
	public final class ReducerExample extends Sprite 
	{
		[Embed(source='picture.png')]
		private static const BITMAP_ASSET: Class;
		
		private var _phase: Number = 0.0;
		private var _shape: Shape;
		
		public function ReducerExample()
		{
			stage.quality = StageQuality.LOW;
			stage.scaleMode = StageScaleMode.NO_SCALE;
			stage.fullScreenSourceRect = new Rectangle( 0.0, 0.0, 800.0, 600.0 );
			stage.frameRate = 32.0;

			addChild( _shape = new Shape() );
			addChild( new BITMAP_ASSET() );
			
			addEventListener(Event.ENTER_FRAME, onEnterFrame);
		}
		
		private function onEnterFrame( event: Event ): void
		{
			var graphics: Graphics = _shape.graphics;
			
			_phase += 0.01;
			
			if( _phase > 1.0 )
				--_phase;
				
			var color: int = Math.sin( _phase * Math.PI ) * 0xff;
			
			color |= color << 0x08;
			color |= color << 0x08;

			graphics.clear();
			graphics.beginFill( color );
			graphics.drawRect( 0.0, 0.0, 800.0, 600.0 );
			graphics.endFill();
		}
	}
}
