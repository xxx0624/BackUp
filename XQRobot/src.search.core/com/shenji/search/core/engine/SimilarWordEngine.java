package com.shenji.search.core.engine;

public interface SimilarWordEngine extends Engine{
	public double getSimilar(String wordA, String wordB);
}
