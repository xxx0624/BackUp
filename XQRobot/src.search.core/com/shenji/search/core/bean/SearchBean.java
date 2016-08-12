package com.shenji.search.core.bean;

public abstract class SearchBean {
	public static class Field {
		public static final String URI = "uri";
	}

	private float score = -1;
	private String uri;

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}
}
