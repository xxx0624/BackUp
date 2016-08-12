package com.shenji.search.core.engine;

import com.shenji.search.core.exception.EngineException;

public interface CustomWordEngine extends Engine {
	public boolean isCustomWord(String word);

	public String[] mixCuttingEnCh(String content);

	public void reset() throws EngineException;
}
