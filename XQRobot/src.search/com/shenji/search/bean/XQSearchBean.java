package com.shenji.search.bean;

import com.shenji.search.core.bean.SearchBean;

public class XQSearchBean extends SearchBean {
	public static class Field extends SearchBean.Field {
		public static final String QUESTION = "question";
		public static final String ANSWER = "answer";
		public static final String TAG1 = "tag1";
		public static final String TAG2 = "tag2";
		public static final String FileSuffixes = ".htm";
	}

	private double similarity;
	private String question;
	private String answer;
	private String htmlContent;
	private String tag1;
	private String tag2;


	public String getTag1() {
		return tag1;
	}

	public void setTag1(String tag1) {
		this.tag1 = tag1;
	}

	public String getTag2() {
		return tag2;
	}

	public void setTag2(String tag2) {
		this.tag2 = tag2;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public double getSimilarity() {
		return similarity;
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

	public String getHtmlContent() {
		return htmlContent;
	}

	public void setHtmlContent(String content) {
		this.htmlContent = content;
	}

	@Override
	public String toString() {
		return "XQSearchBean [similarity=" + similarity + ", question="
				+ question + ", answer=" + answer + ", htmlContent="
				+ htmlContent + ", tags1=" + tag1 + ", tags2=" + tag2 + "]";
	}
	
}
