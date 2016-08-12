package com.shenji.search.core.engine;

import com.shenji.search.core.exception.EngineException;

public interface SynonymEngine extends Engine{
	public static String WORD="word";
	public static String SYNONM="syn";
	
	String[] getSynonyms(String s) throws EngineException;

}
