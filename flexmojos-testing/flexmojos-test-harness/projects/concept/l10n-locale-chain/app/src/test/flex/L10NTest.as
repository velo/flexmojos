/**
 * @author Seven
 */
package
{

	import flexunit.framework.Assert;

	import mx.resources.*;

	[ResourceBundle("text")]
	[ResourceBundle("collections")]
	public class L10NTest
	{

		[Test]
		public function addition():void
		{
			var rm:IResourceManager=ResourceManager.getInstance();
			rm.localeChain=['en_US'];
			Assert.assertEquals('Main View', rm.getString("text", "TITLE", null, "en_US"));
			Assert.assertEquals('Vista principal', rm.getString("text", "TITLE", null, "pt_PT"));

//noItems fall back from pt_PT to pt_BR
			Assert.assertEquals('No items to search.', rm.getString("collections", "noItems", null, "en_US"));
			Assert.assertEquals('Nenhum item a ser pesquisado.', rm.getString("collections", "noItems", null, "pt_PT"));
		}

	}

}