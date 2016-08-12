package com.shenji.robot.webservices.demo;

import java.io.IOException;

import org.jsoup.Jsoup;

import com.shenji.robot.webservices.port.Search;
import com.shenji.search.control.IEnumSearch;

public class DSearch {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		//dRebuildIndex();
		
		
		
		//daddNewFAQ();
		//drebuildIndex();
		//dFenci();
		//ddelFAQ();
		dSearch();
		//Thread.sleep(10000);
		//dSearch();
		//daddPhrase();
		
		// dSearchNum();
		//org.jsoup.nodes.Document doc = Jsoup.connect("http://localhost:8888/XQDoc/document/faq/e8691b7a31d4d3057a053a2c541784f5.htm").get();
	}

	
	
	public static void dRebuildIndex() {
		Search search = new Search();
		search.rebuildIndex();
	}

	public static void dFenci() {
		Search search = new Search();
		String str = "证书更新后，登录电子报税或网上认证时，提示：登记信息税局验证失败。ca证书错误,为什么？";
		System.out.println(search.fenCi(str,
				IEnumSearch.Fenci.MORE_NOSYN.value()));
		System.out.println(search.fenCi(str,
				IEnumSearch.Fenci.MAX_NOSYN.value()));
		
		System.out.println(search.fenCi(str,
				IEnumSearch.Fenci.MORE_SYN.value()));
		System.out.println(search.fenCi(str,
				IEnumSearch.Fenci.MAX_SYN.value()));
	}

	public static void dSearch() {
		Search search = new Search();
		String html = search.searchHtml("网上认证",
				IEnumSearch.SearchRelationType.OR_SEARCH.value(),
				IEnumSearch.SearchConditionType.FilterByOnto.value());
		System.out.println(html);
	}

	public static void daddNewFAQ() {
		Search search = new Search();
		System.out.println(search.addNewFAQ(new String[] { "毕福剑是傻逼吗？" },
				new String[] { "毕福剑不是，他是大帅比！" }));
	}
	
	public static void ddelFAQ() {
		Search search = new Search();
		System.out.println(search.deleteFAQ(new String[] { "http://localhost:8888/XQDoc/document/faq/2a9e9ee47525d8eee5f853b9dda1e279.htm"}));
	}
	
	
	
	public static void daddPhrase() {
		Search search = new Search();
		System.out.println(search.addPhrase("郭宏伟", "大帅比"));
	}

	public static void drebuildIndex() {
		Search search = new Search();
		search.rebuildIndex();
	}

	public static void dSearchNum() {
		Search search = new Search();
		String[] reStrs = search.searchNum("毕福剑", 3,
				IEnumSearch.SearchRelationType.OR_SEARCH.value(),
				IEnumSearch.SearchConditionType.Ordinary.value());
		for (String s : reStrs) {
			System.out.println(s);
		}
		System.out.println(reStrs.length);
	}

}
