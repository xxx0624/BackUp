package com.shenji.search.control;

import java.io.File;
import java.io.IOException;

import com.shenji.common.log.Log;
import com.shenji.common.util.IniEditor;
import com.shenji.common.util.PathUtil;

public class Configuration {
	static {
		init();
	}

	public static void load() {
	}

	public final static String configFile = "conf/search_conf.ini";
	public final static String parameter_config = "parameter_config";
	public final static String system_config = "system_config";
	public final static String dic_config = "dic_config";
	// public final static String database_config = "database_config";

	public static String IP;
	public static String notesName;

	public static String notesPath;
	public static String indexPath;
	public static String faqFolder;
	public static String learnFolder;
	public static String webFolder;
	public static String synFolder;
	public static String mySynFolder;
	public static String relatedWordFolder;

	public static String myDict;
	public static String businessDict;
	public static String synonmDict;
	public static String mySynonmDict;
	// xxx0624
	public static String faqFolderHowQ;
	public static String faqFolderWhyQ;
	public static String faqFolderWhereQ;
	public static String faqFolderWhenQ;
	public static String faqFolderWhatQ;
	public static String faqFolderOrQ;

	/*
	 * public static String dbIP; public static String dbName; public static
	 * String user = "root"; public static String password = "";
	 */

	public static void init() {
		IniEditor editor = new IniEditor();
		String path;
		try {
			path = PathUtil.getWebInFAbsolutePath();
			if (new File(path + configFile).exists()) {
				editor.load(path + configFile);
				IP = editor.get(system_config, "IP");
				notesName = editor.get(system_config, "notesName");
				notesPath = editor.get(system_config, "notesPath");
				// notesPath=notesPath.replace("\\\\", "\\");
				indexPath = editor.get(system_config, "indexPath");
				// indexPath=indexPath.replace("\\\\", "\\");
				faqFolder = editor.get(system_config, "faqFolder");
				learnFolder = editor.get(system_config, "learnFolder");
				webFolder = editor.get(system_config, "webFolder");
				synFolder = editor.get(system_config, "synFolder");
				mySynFolder = editor.get(system_config, "mySynFolder");
				relatedWordFolder = editor.get(system_config,
						"relatedWordFolder");
				myDict = editor.get(dic_config, "myDict");
				businessDict = editor.get(dic_config, "businessDict");
				synonmDict = editor.get(dic_config, "synonmDict");
				mySynonmDict = editor.get(dic_config, "MySynonmDict");

				// xxx0624
				faqFolderHowQ = editor.get(system_config, "faqFolderHowQ");
				faqFolderWhyQ = editor.get(system_config, "faqFolderWhyQ");
				faqFolderWhereQ = editor.get(system_config, "faqFolderWhereQ");
				faqFolderWhenQ = editor.get(system_config, "faqFolderWhenQ");
				faqFolderWhatQ = editor.get(system_config, "faqFolderWhatQ");
				faqFolderOrQ = editor.get(system_config, "faqFolderOrQ");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.getLogger(Configuration.class).error(e.getMessage(), e);
		}
	}

	// 朱
	public static String[] searchIndexDirs = { indexPath + "/" + faqFolder };
	public static String[] searchDocmentDirs = { notesPath + "/" + faqFolder };
	public static String webPath = "http://" + IP + "/" + notesName;
	// xxx0624
	public static String[] searchIndexDirsHowQ = { indexPath + "/"
			+ faqFolderHowQ };
	public static String[] searchDocmentDirsHowQ = { notesPath + "/"
			+ faqFolderHowQ };
	public static String[] searchIndexDirsWhyQ = { indexPath + "/"
			+ faqFolderWhyQ };
	public static String[] searchDocmentDirsWhyQ = { notesPath + "/"
			+ faqFolderWhyQ };
	public static String[] searchIndexDirsWhereQ = { indexPath + "/"
			+ faqFolderWhereQ };
	public static String[] searchDocmentDirsWhereQ = { notesPath + "/"
			+ faqFolderWhereQ };
	public static String[] searchIndexDirsWhenQ = { indexPath + "/"
			+ faqFolderWhenQ };
	public static String[] searchDocmentDirsWhenQ = { notesPath + "/"
			+ faqFolderWhenQ };
	public static String[] searchIndexDirsWhatQ = { indexPath + "/"
			+ faqFolderWhatQ };
	public static String[] searchDocmentDirsWhatQ = { notesPath + "/"
			+ faqFolderWhatQ };
	public static String[] searchIndexDirsOrQ = { indexPath + "/"
			+ faqFolderOrQ };
	public static String[] searchDocmentDirsOrQ = { notesPath + "/"
			+ faqFolderOrQ };
	/*
	 * public static String faqwebPath = "http://" + IP + notesName +
	 * File.separator + faqFolder;
	 */

}
