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
