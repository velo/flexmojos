package org.sonatype.mather 
{
	
	public class Mather
	{
		
		public static function add(...values):Number {
			var result:Number = 0;
			for each (var value:* in values) {
				if(!(value is Number)) {
					throw new Error ("Invalid number: " + value);
				}
				result += value;
			}
			
			return result;
		}
		
	}

}
