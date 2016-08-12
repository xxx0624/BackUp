package com.shenji.search.core.old;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.shenji.search.core.exception.EngineException;
import com.shenji.search.core.exception.SearchException;

public class Test {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		new Test().init(new String[] { "D:/FAQ", "D:/FAQLog", "hh", "kl" });
	}

	public void init(String[] indexFolders) throws Exception {
		int size = indexFolders.length;
		try {
			int i = 1;
		} catch (Exception e) {
			throw new EngineException("文本分析器构造失败!", e);
		}
		Exception indexFileException = null;
		for (String indexFolder : indexFolders) {
			File file = new File(indexFolder);
			if (!file.exists()) {
				String message = "【索引文件夹:" + indexFolder + "不存在！】";
				indexFileException=packageException(indexFileException,message);
				continue;
			}
			try {
				int i = 1 / 0;
			} catch (Exception e) {
				String message = "【索引文件夹:" + indexFolder + "打开失败！】";
				Exception ex = new SearchException(message, e,
						SearchException.ErrorCode.LuceneError);
				indexFileException=packageException(indexFileException,message);
				continue;
			}
		}
		if (indexFileException != null) {
			throw indexFileException;
		}
	}
	
	private Exception packageException(Exception ex,String message){
		if (ex != null)
			ex = new SearchException(message, ex,
					SearchException.ErrorCode.LuceneError);
		else
			ex = new SearchException(message,
					SearchException.ErrorCode.LuceneError);
		return ex;
	}

}
