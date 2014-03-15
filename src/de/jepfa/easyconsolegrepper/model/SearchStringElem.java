package de.jepfa.easyconsolegrepper.model;


/**
 * An Search String Element. An element is equals when only the searc hstring is equals. Attribute stamp is only for sorting.
 *
 * @author Jens Pfahl
 */
public class SearchStringElem implements Comparable<SearchStringElem> {

	private int stamp;
	private String searchString;



	public SearchStringElem(int stamp, String searchString) {
		super();
		this.stamp = stamp;
		this.searchString = searchString;
	}




	public int getStamp() {
		return stamp;
	}


	public String getSearchString() {
		return searchString;
	}




	@Override
	public String toString() {
		return "SearchStringElem [stamp=" + stamp + ", searchString="
				+ searchString + "]";
	}




	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((searchString == null) ? 0 : searchString.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchStringElem other = (SearchStringElem) obj;
		if (searchString == null) {
			if (other.searchString != null)
				return false;
		} else if (!searchString.equals(other.searchString))
			return false;
		return true;
	}


	@Override
	public int compareTo(SearchStringElem o) {
		return this.getStamp() > o.getStamp() ? -1 : this.getStamp() == o.getStamp() ? 0 : 1;
	}

}