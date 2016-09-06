package com.shenji.web.bean;

public class QALogBean {
	private int id;
	
	private String userQuestion;
	
	private String robotQuestion;
	
	private String robotAnswer;
	
	private int sortNum;
	
	private String score;
	
	private int qaType;
	
	private String logId;

	public String getLogId() {
		return logId;
	}

	public void setLogId(String logId) {
		this.logId = logId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUserQuestion() {
		return userQuestion;
	}

	public void setUserQuestion(String userQuestion) {
		this.userQuestion = userQuestion;
	}

	public String getRobotQuestion() {
		return robotQuestion;
	}

	public void setRobotQuestion(String robotQuestion) {
		this.robotQuestion = robotQuestion;
	}

	public String getRobotAnswer() {
		return robotAnswer;
	}

	public void setRobotAnswer(String robotAnswer) {
		this.robotAnswer = robotAnswer;
	}

	public int getSortNum() {
		return sortNum;
	}

	public void setSortNum(int sortNum) {
		this.sortNum = sortNum;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public int getQaType() {
		return qaType;
	}

	public void setQaType(int qaType) {
		this.qaType = qaType;
	}

	public QALogBean(int id, String userQuestion, String robotQuestion,
			String robotAnswer, int sortNum, String score, int qaType) {
		this.id = id;
		this.userQuestion = userQuestion;
		this.robotQuestion = robotQuestion;
		this.robotAnswer = robotAnswer;
		this.sortNum = sortNum;
		this.score = score;
		this.qaType = qaType;
	}
	
	public QALogBean(String userQuestion, String robotQuestion,
			String robotAnswer, int sortNum, String score, int qaType,
			String logId) {
		this.userQuestion = userQuestion;
		this.robotQuestion = robotQuestion;
		this.robotAnswer = robotAnswer;
		this.sortNum = sortNum;
		this.score = score;
		this.qaType = qaType;
		this.logId = logId;
	}
	
	public QALogBean(){
	}
}
