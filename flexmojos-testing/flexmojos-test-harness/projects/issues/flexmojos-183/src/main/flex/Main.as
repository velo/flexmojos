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
    import flash.display.Sprite;
    import flash.events.Event;
    import flash.text.Font;
    import flash.text.TextField;
    import flash.text.TextFieldAutoSize;
    import flash.text.TextFormat;

    public class Main extends Sprite
    {

      public function Main():void
      {
        if (stage) init();
        else addEventListener(Event.ADDED_TO_STAGE, init);
      }

      [Embed(source="/myFont.swf",      symbol="myFont")]
      private var _fontClass:Class;

      private var _alias:String = "myComic";

      private function init(e:Event = null):void
      {
        removeEventListener(Event.ADDED_TO_STAGE, init);

        Font.registerFont(_fontClass);

        var tf:TextField = new TextField();
        tf.defaultTextFormat = new TextFormat(_alias);
        tf.embedFonts = true;
        tf.autoSize = TextFieldAutoSize.LEFT
        tf.text = "The quick brown fox jumps over the lazy dog. 1234567890";

        addChild(tf);

CFG::IT {
        System.exit( 3539 );
}

      }
    }
}
