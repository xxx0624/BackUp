package com.shenji.search.core.engine;

import com.shenji.search.core.exception.EngineException;

public interface BusinessEngine extends Engine{
	/**
	 * 得到某个词的权重
	 * @param word 词
	 * @return 权重
	 * @throws EngineException
	 */
	float getWeight(String word) throws EngineException;
}
