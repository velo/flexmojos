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
package info.rvin.mojo.flexmojo.compiler;

import java.util.ArrayList;
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
	private List<String> contributors;

	/**
	 * A creator's name to store in the SWF metadata (repeatable)
	 */
	private List<String> creators;

	/**
	 * The creation date to store in the SWF metadata
	 */
	private String date;

	/**
	 * The default description to store in the SWF metadata
	 */
	private String description;

	/**
	 * The language to store in the SWF metadata (i.e. EN, FR) (repeatable)
	 */
	private List<String> languages;

	/**
	 * -metadata.localized-description <text> <lang>
	 *
	 * A localized RDF/XMP description to store in the SWF metadata (repeatable)
	 */
	private Map<String, String> localizedDescription;

	/**
	 * -metadata.localized-title <title> <lang>
	 *
	 * A localized RDF/XMP title to store in the SWF metadata (repeatable)
	 */
	private Map<String, String> localizedTitle;

	/**
	 * A publisher's name to store in the SWF metadata (repeatable)
	 */
	private List<String> publishers;

	/**
	 * The default title to store in the SWF metadata
	 */
	private String title;

	public List<String> getContributors() {
		return contributors;
	}

	public void setContributors(List<String> contributors) {
		this.contributors = contributors;
	}

	public List<String> getCreators() {
		return creators;
	}

	public void setCreators(List<String> creators) {
		this.creators = creators;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getLanguages() {
		return languages;
	}

	public void setLanguages(List<String> languages) {
		this.languages = languages;
	}

	public Map<String, String> getLocalizedDescription() {
		return localizedDescription;
	}

	public void setLocalizedDescription(Map<String, String> localizedDescription) {
		this.localizedDescription = localizedDescription;
	}

	public Map<String, String> getLocalizedTitle() {
		return localizedTitle;
	}

	public void setLocalizedTitle(Map<String, String> localizedTitle) {
		this.localizedTitle = localizedTitle;
	}

	public List<String> getPublishers() {
		return publishers;
	}

	public void setPublishers(List<String> publishers) {
		this.publishers = publishers;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void addContributor(String name) {
		if (this.contributors == null) {
			this.contributors = new ArrayList<String>();
		}
		this.contributors.add(name);
	}

	public void addCreator(String name) {
		if (this.creators == null) {
			this.creators = new ArrayList<String>();
		}
		this.creators.add(name);
	}

}
