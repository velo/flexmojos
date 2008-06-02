package info.rvin.mojo.flexmojo.compiler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class hold SWF metadata
 *
 * @author velo
 *
 */
public class Metadata {

	/**
	 * A contributor's name to store in the SWF metadata (repeatable)
	 */
	private String contributor;

	/**
	 * A creator's name to store in the SWF metadata (repeatable)
	 */
	private String creator;

	/**
	 * The creation date to store in the SWF metadata
	 */
	private String date;

	/**
	 * The default description to store in the SWF metadata
	 */
	private Map<String, String> descriptions;

	/**
	 * The language to store in the SWF metadata (i.e. EN, FR) (repeatable)
	 */
	private String language;

	/**
	 * A publisher's name to store in the SWF metadata (repeatable)
	 */
	private List<String> publishers;

	/**
	 * The default title to store in the SWF metadata
	 */
	private Map<String, String> titles;

	public String getContributor() {
		return contributor;
	}

	public void setContributor(String contributor) {
		this.contributor = contributor;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void addDescription(String locale, String description) {
		if (descriptions == null) {
			descriptions = new HashMap<String, String>();
		}
		descriptions.put(locale, description);
	}

	public Map<String, String> getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(Map<String, String> description) {
		this.descriptions = description;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String languages) {
		this.language = languages;
	}

	public List<String> getPublishers() {
		return publishers;
	}

	public void setPublishers(List<String> publishers) {
		this.publishers = publishers;
	}

	public void addTitle(String locale, String title) {
		if (titles == null) {
			titles = new HashMap<String, String>();
		}
		titles.put(locale, title);
	}

	public Map<String, String> getTitles() {
		return titles;
	}

	public void setTitles(Map<String, String> titles) {
		this.titles = titles;
	}

}
