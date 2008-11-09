package info.flexmojos.unitestingsupport.util
{
	import flash.utils.describeType;
	
	public class ClassnameUtil
	{
		public function ClassnameUtil()
		{
		}
		
		/**
		 * Return the fully qualified class name for an Object.
		 * @param obj the Object.
		 * @return the class name.
		 */
		public static function getClassName( obj:Object ):String
		{
			var description:XML = describeType( obj );
			var className:Object = description.@name;
			
			return className[ 0 ];
		}
	}
}