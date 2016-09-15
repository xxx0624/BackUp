package com.shenji.search.core.bean;

public class ExplainBean {
	
	private String tf;
	private String idf;
	private String fieldNorm;
	private String boost;
	private String allScore;
	
	public String getTf() {
		return tf;
	}
	public void setTf(String tf) {
		this.tf = tf;
	}
	public String getIdf() {
		return idf;
	}
	public void setIdf(String idf) {
		this.idf = idf;
	}
	public String getFieldNorm() {
		return fieldNorm;
	}
	public void setFieldNorm(String fieldNorm) {
		this.fieldNorm = fieldNorm;
	}
	public String getBoost() {
		return boost;
	}
	public void setBoost(String boost) {
		this.boost = boost;
	}
	public String getAllScore() {
		return allScore;
	}
	public void setAllScore(String allScore) {
		this.allScore = allScore;
	}
	
	
}
