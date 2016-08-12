package com.shenji.search.core.dic;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.shenji.common.log.Log;
import com.shenji.common.util.FileUse;
import com.shenji.common.util.PathUtil;
import com.shenji.search.control.Configuration;
import com.shenji.search.core.engine.BusinessEngine;
import com.shenji.search.core.exception.EngineException;

/**
 * @author zhq
 * 带权重的词典
 */
public class BusinessDic implements BusinessEngine {
	private static HashMap<String, Float> businessDictMap;
	private static BusinessDic instance;

	private BusinessDic() throws EngineException {
		businessDictMap = new HashMap<String, Float>();
		try {
			loadBusinessDict();
		} catch (Exception e) {
			// TODO: handle exception
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
		}

	}

	public static synchronized BusinessDic getInstance() throws EngineException {
		if (instance == null) {
			synchronized (BusinessDic.class) {
				if (instance == null)
					instance = new BusinessDic();
			}
		}
		return instance;
	}

	public float getWeight(String word) throws EngineException {
		// TODO Auto-generated method stub
		if (businessDictMap.get(word) != null)
			return businessDictMap.get(word);
		else
			return -1;
	}

	/**
	 * 加载带权重的词典
	 * @throws EngineException
	 */
	private void loadBusinessDict() throws EngineException {
		String path = PathUtil.getWebInFAbsolutePath();
		File file = new File(path + Configuration.businessDict);
		if (!file.exists())
			throw new EngineException("businessDict File is Null!");
		List<String> list = FileUse.read(file);
		for (String s : list) {
			String[] strings = s.split(" ");
			businessDictMap.put(strings[0], Float.valueOf(strings[1]));
		}
		list.clear();
	}

	/**
	 * 重置带权重的词典
	 * @throws EngineException
	 */
	public void reset() throws EngineException {
		businessDictMap.clear();
		try {
			loadBusinessDict();
		} catch (Exception e) {
			// TODO: handle exception
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
		}
	}

	/**
	 * 添加带权重的词
	 * @param word 词
	 * @param weight 权重
	 * @return 1 成功 -1 失败
	 * @throws EngineException
	 */
	public int addNewBusinessWord(String word, float weight)
			throws EngineException {
		String path;
		int result;
		try {
			path = PathUtil.getWebInFAbsolutePath();
			File file = new File(path + Configuration.businessDict);
			String str = word + " " + weight;
			result = FileUse.write(file, FileUse.add(file, str));
		} catch (Exception e) {
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
			return -1;
		}
		try {
			reset();
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			throw new EngineException("Reset BusinessDic Exception!", e);
		}
		return result;
	}

	/**
	 * 修改带权重的词
	 * @param oldWord 旧词
	 * @param newWord 新词
	 * @param newWeight 新权重
	 * @return 1  成功 -1 失败
	 * @throws EngineException
	 */
	public int modifyBusinessWord(String oldWord, String newWord,
			float newWeight) throws EngineException {
		String path;
		int result;
		path = PathUtil.getWebInFAbsolutePath();
		String str = newWord + " " + newWeight;
		result = FileUse.write(path, Configuration.businessDict, FileUse
				.modify(path, Configuration.businessDict, oldWord, str,
						FileUse.CONTAINS_TYPE));
		try {
			reset();
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			throw new EngineException("Reset BusinessDic Exception!", e);
		}
		return result;
	}

	/**
	 * 删除带权重的词
	 * @param word 词
	 * @return 1  成功 -1 失败
	 * @throws EngineException
	 */
	public int deleteBusinessWord(String word) throws EngineException {
		String[] words = { word };
		String path;
		int result;
		path = PathUtil.getWebInFAbsolutePath();
		result = FileUse.write(path, Configuration.businessDict, FileUse
				.delete(path, Configuration.businessDict, words,
						FileUse.CONTAINS_TYPE));
		try {
			reset();
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			throw new EngineException("Reset BusinessDic Exception!", e);
		}
		return result;
	}

	/**
	 * 列出所有带权重的词
	 * @return 权重词集
	 */
	public String[] listBusinessDict() {
		if (businessDictMap == null || businessDictMap.size() == 0)
			return null;
		String[] strs = new String[businessDictMap.size()];
		int count = 0;
		Iterator<Map.Entry<String, Float>> iterator = businessDictMap
				.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Float> entry = iterator.next();
			strs[count++] = entry.getKey() + "/" + entry.getValue();

		}
		return strs;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	
}
