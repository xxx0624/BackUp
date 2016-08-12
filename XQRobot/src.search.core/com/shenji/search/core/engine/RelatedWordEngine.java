package com.shenji.search.core.engine;

import java.util.Map;

public interface RelatedWordEngine extends Engine{
	public static final double minRalatedNum = 0.6;
	public Map<String, Double> getRelatedWord(String word);
}
