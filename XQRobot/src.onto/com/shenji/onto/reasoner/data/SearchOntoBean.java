package com.shenji.onto.reasoner.data;

import com.shenji.search.bean.XQSearchBean;

public class SearchOntoBean extends XQSearchBean{
	public SearchOntoBean(){		
	}
	
	public SearchOntoBean(XQSearchBean bean){
		super();
		this.setAnswer(bean.getAnswer());
		this.setHtmlContent(bean.getHtmlContent());
		this.setUri(bean.getUri());
		this.setQuestion(bean.getQuestion());
		this.setSimilarity(bean.getSimilarity());
		this.setScore(bean.getScore());
	}
	/**
	 * 本体维度（相似度）
	 */
	private double ontoDimension=1;

	public double getOntoDimension() {
		return ontoDimension;
	}

	public void setOntoDimension(double ontoDimension) {
		this.ontoDimension = ontoDimension;
	} 
}
