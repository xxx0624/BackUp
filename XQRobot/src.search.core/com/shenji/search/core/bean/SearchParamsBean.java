package com.shenji.search.core.bean;

import java.util.Set;

public class SearchParamsBean {
	protected String sentence=null;
	protected String fromFolder=null;
	protected Set<String> queryFiledArray = null;
	protected Set<String> fineGrainedParticipleArray = null;
	protected Set<String> coarseGrainedParticipleArray = null;
	protected ESearchRelation relation;
	public SearchParamsBean(String sentence,ESearchRelation relation,
			Set<String> fineGrainedParticipleArray,
			Set<String> coarseGrainedParticipleArray) {
		this.sentence = sentence;
		this.relation=relation;
		this.fineGrainedParticipleArray = fineGrainedParticipleArray;
		this.coarseGrainedParticipleArray = coarseGrainedParticipleArray;
		if(this.relation.equals(ESearchRelation.AND_SEARCH)){
			this.queryFiledArray = coarseGrainedParticipleArray;
		}
		else		
			this.queryFiledArray = fineGrainedParticipleArray;
	}

	/*public SearchParamsBean() {
		// TODO Auto-generated constructor stub
	}*/
	public ESearchRelation getRelation() {
		return relation;
	}

	public void clear() {
		if (this.fineGrainedParticipleArray != null) {
			this.fineGrainedParticipleArray.clear();
		}
		if (this.coarseGrainedParticipleArray != null) {
			this.coarseGrainedParticipleArray.clear();
		}
		if (this.queryFiledArray != null) {
			this.queryFiledArray.clear();
		}
	}

	public void setRelation(ESearchRelation relation) {
		this.relation = relation;
	}

	public String getSentence() {
		return sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	public String getFromFolder() {
		return fromFolder;
	}

	public void setFromFolder(String fromFolder) {
		this.fromFolder = fromFolder;
	}

	public Set<String> getQueryFiledArray() {
		return queryFiledArray;
	}

	public void setQueryFiledArray(Set<String> queryFiledArray) {
		this.queryFiledArray = queryFiledArray;
	}
	
	public Set<String> getFineGrainedParticipleArray() {
		return fineGrainedParticipleArray;
	}

	public void setFineGrainedParticipleArray(Set<String> fineGrainedParticipleArray) {
		this.fineGrainedParticipleArray = fineGrainedParticipleArray;
	}

	public Set<String> getCoarseGrainedParticipleArray() {
		return coarseGrainedParticipleArray;
	}

	public void setCoarseGrainedParticipleArray(
			Set<String> coarseGrainedParticipleArray) {
		this.coarseGrainedParticipleArray = coarseGrainedParticipleArray;
	}

}
