package com.shenji.search.test;

import com.shenji.search.control.ResourcesControl;
import com.shenji.search.control.SearchControl;
import com.shenji.search.core.bean.ESearchRelation;
import com.shenji.search.core.exception.SearchException;
import com.shenji.search.core.search.Search;

public class IndexTest {
	public static void main(String[] str) throws SearchException {
		ResourcesControl control = new ResourcesControl();
		/*
		 * try { control.rebuildIndex(); } catch (Exception e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */
		// System.out.println(control.addNewFAQ(new String[]{"3241234234"}, new
		// String[]{"3241234234"}));
		// System.out.println(new SearchControl().searchBasic("3241234234",
		// ESearchRelation.OR_SEARCH));
		control.rebuildIndex();

	}
}
