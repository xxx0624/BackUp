package com.shenji.search.bean;

public class WordLogBean {
	int id;
	private String user_question;
	private String add_time;
	private String user_question_word;
	private String user_question_score;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUser_question() {
		return user_question;
	}
	public void setUser_question(String user_question) {
		this.user_question = user_question;
	}
	public String getAdd_time() {
		return add_time;
	}
	public void setAdd_time(String add_time) {
		this.add_time = add_time;
	}
	public String getUser_question_word() {
		return user_question_word;
	}
	public void setUser_question_word(String user_question_word) {
		this.user_question_word = user_question_word;
	}
	public String getUser_question_score() {
		return user_question_score;
	}
	public void setUser_question_score(String user_question_score) {
		this.user_question_score = user_question_score;
	}
	public WordLogBean(String user_question, String add_time,
			String user_question_word, String user_question_score) {
		super();
		this.user_question = user_question;
		this.add_time = add_time;
		this.user_question_word = user_question_word;
		this.user_question_score = user_question_score;
	}
	
	public WordLogBean(String user_question_word, String user_question_score) {
		super();
		this.user_question_word = user_question_word;
		this.user_question_score = user_question_score;
	}
	
}
