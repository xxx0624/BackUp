package org.wltea.analyzer.lucene;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

import com.shenji.search.core.engine.SynonymEngine;

public class SynonymAnalyzer extends Analyzer {
	// 同义词引擎
	private SynonymEngine engine;

	public SynonymAnalyzer(SynonymEngine engine) {
		this.engine = engine;
	}

	@Override
	public TokenStream tokenStream(String arg, Reader reader) {
		// IK中文分词流
		TokenStream ikStream = new IKTokenizer(reader, false);
		// 同义词中文分词流（包装了IK中文分词流）
		TokenStream synStream = new SynonymFilter(ikStream, engine);
		return synStream;
	}
}
