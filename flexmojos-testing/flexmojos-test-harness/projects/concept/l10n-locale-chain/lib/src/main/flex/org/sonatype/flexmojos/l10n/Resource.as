package org.sonatype.flexmojos.l10n
{
	import mx.resources.ResourceManager;

	[ResourceBundle("text")]
	public class Resource
	{

		public var title:String;

		public function Resource()
		{
			title = ResourceManager.getInstance().getString("text", "TITLE");
		}

	}
}