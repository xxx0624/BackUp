package com.shenji.robot.inter;

import com.shenji.search.core.exception.EngineException;

public interface IComFenciServer {
	public String iKAnalysis(String str);

	public String iKAnalysisAndSyn(String str) throws EngineException;

	public String[] iKAnalysisWithPossibility(String str)
			throws EngineException;
}