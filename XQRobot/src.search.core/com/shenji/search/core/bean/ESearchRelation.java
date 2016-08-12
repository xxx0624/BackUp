package com.shenji.search.core.bean;


public enum ESearchRelation {
	OR_SEARCH(1), AND_SEARCH(2);
	private int value;

	private ESearchRelation(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

	public static ESearchRelation valueOf(int value)
			throws IllegalArgumentException { // 手写的从int到enum的转换函数
		for (ESearchRelation t : ESearchRelation.values()) {
			if (t.value() == value) {
				return t;
			}
		}
		throw new IllegalArgumentException(
				"Enum SearchRelationType Argument is Error!");
	}
}
