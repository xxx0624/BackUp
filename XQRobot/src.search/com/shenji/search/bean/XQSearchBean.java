package com.shenji.search.bean;

import com.shenji.search.core.bean.SearchBean;

public class XQSearchBean extends SearchBean {
	public static class Field extends SearchBean.Field {
		public static final String QUESTION = "question";
		public static final String ANSWER = "answer";
		public static final String FileSuffixes = ".htm";
	}

	private double similarity;
	private String question;
	private String answer;
	private String htmlContent;

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
}
