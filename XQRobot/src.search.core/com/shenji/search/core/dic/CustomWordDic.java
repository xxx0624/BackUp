package com.shenji.search.core.dic;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.shenji.common.util.FileUse;
import com.shenji.common.util.PathUtil;
import com.shenji.search.control.Configuration;
import com.shenji.search.core.engine.CustomWordEngine;
import com.shenji.search.core.exception.EngineException;

import com.shenji.common.log.Log;

public class CustomWordDic implements CustomWordEngine {
	private static Set<String> customDic;
	private static Set<String> costomEnChDic;

	private CustomWordDic() throws EngineException {
		Log.getLogger().info("init: loading myDict.dic");
		init();
		// 做一次可以了、、以后注释掉吧
		// removeDictDuplicate();
	}

	private static CustomWordDic instance;

	public static synchronized CustomWordDic getInstance()
			throws EngineException {
		if (instance == null) {
			synchronized (CustomWordDic.class) {
				if (instance == null)
					instance = new CustomWordDic();
			}
		}
		return instance;
	}

	public boolean isCustomWord(String word) {
		if (customDic.contains(word)) {
			return true;
		} else
			return false;
	}

	public String[] mixCuttingEnCh(String content) {
		//不分大小写
		List<String> list = new ArrayList<String>();
		for (String s : CustomWordDic.costomEnChDic) {
			if (content.contains(s) || content.contains(s.toLowerCase())) {
				list.add(s);
			}
		}
		return (String[]) list.toArray(new String[list.size()]);
	}

	private void init() throws EngineException {
		customDic = new LinkedHashSet<String>();
		costomEnChDic = new LinkedHashSet<String>();
		String path;
		path =  PathUtil.getWebInFAbsolutePath();
		File file = new File(path + Configuration.myDict);
		if (!file.exists())
			throw new EngineException("CustomWordDic file is not Exists!");
		List<String> list = FileUse.read(file);
		for (String s : list) {
			s = s.trim();
			customDic.add(s);
			if (FileUse.isNotChineseWord(s)) {
				costomEnChDic.add(s);
			}
		}

	}

	public void reset() throws EngineException {
		customDic.clear();
		costomEnChDic.clear();
		instance.init();
	}

	private static void removeDictDuplicate() {
		String myDictPath = null;
		myDictPath =  PathUtil.getWebInFAbsolutePath();
		ArrayList<String> list = new ArrayList<String>(customDic);
		FileUse.write(myDictPath, Configuration.myDict, list);
		list.clear();
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
	}

}
