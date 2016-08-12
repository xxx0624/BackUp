package com.shenji.search.core.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.SynonymAnalyzer;

import com.shenji.common.log.Log;
import com.shenji.search.core.bean.SearchBean;
import com.shenji.search.core.dic.CommonSynonymDic;
import com.shenji.search.core.engine.SynonymEngine;
import com.shenji.search.core.exception.EngineException;
import com.shenji.search.core.exception.SearchException;

public abstract class SearchIndex {
	private List<IndexWriter> writers = null;
	private List<Directory> directorys = null;
	private Analyzer analyzer = null;
	private SynonymEngine engine = null;

	public SearchIndex(String[] indexFolders) throws Exception {
		if (indexFolders.length == 0)
			throw new SearchException("文件夹不能为空!",
					SearchException.ErrorCode.LuceneError);
		this.init(indexFolders);
	}

	public void close() {
		try {
			for (IndexWriter writer : writers) {
				if (writer != null) {
					writer.close();
				}
			}
			for (Directory directory : directorys) {
				if (directory != null) {
					directory.close();
				}
			}
			if (engine != null)
				engine.close();
			if (analyzer != null)
				analyzer.close();
		} catch (Exception e) {
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
		}
	}

	private Exception packageException(Exception ex, Exception e, String message) {
		if (ex != null)
			ex = new SearchException(message, ex,
					SearchException.ErrorCode.LuceneError);
		else
			ex = new SearchException(message, e,
					SearchException.ErrorCode.LuceneError);
		return ex;
	}

	private void init(String[] indexFolders) throws Exception {
		int size = indexFolders.length;
		try {
			engine = new CommonSynonymDic();
			analyzer = new SynonymAnalyzer(engine);
		} catch (EngineException e) {
			throw new EngineException("文本分析器构造失败!", e);
		}
		writers = new ArrayList<IndexWriter>(size);
		this.directorys = new ArrayList<Directory>(size);
		Exception indexFileException = null;
		for (String indexFolder : indexFolders) {
			File file = new File(indexFolder);
			if (!file.exists()) {
				file.mkdirs();
				Log.getLogger(this.getClass()).info("创建索引文件夹:" + indexFolder);
			}
			try {
				Directory directory = FSDirectory.open(file);
				unlockIndex(directory);
				directorys.add(directory);
			} catch (IOException e) {
				String message = "索引文件夹:" + indexFolder + "打开失败！";
				indexFileException = packageException(indexFileException, e,
						message);
			}
		}
		if (indexFileException != null) {
			throw indexFileException;
		}
		for (Directory directory : directorys) {
			try {
				IndexWriter writer = new IndexWriter(directory, analyzer,
						IndexWriter.MaxFieldLength.UNLIMITED);
				writer.setMaxBufferedDocs(500);
				writers.add(writer);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				throw new SearchException("“索引写”对象构造失败!", e,
						SearchException.ErrorCode.LuceneError);
			}

		}
	}

	private void unlockIndex(Directory directory) throws Exception {
		if (IndexWriter.isLocked(directory)) {
			IndexWriter.unlock(directory);
		}
	}

	public int judgeIndexTag(String tag){
		if( tag.equals("faq") )
			return 0;
		else if( tag.equals("how") )
			return 1;
		else if( tag.equals("why") )
			return 2;
		else if( tag.equals("where") )
			return 3;
		else if( tag.equals("when") )
			return 4;
		else if( tag.equals("what") )
			return 5;
		else if( tag.equals("or") )
			return 6;
		else return -1;
	}
	
	protected boolean addIndex(SearchBean bean,String tag) throws Exception {
		Document doc = new Document();
		List<Field> fields = this.getFileds(bean);
		int idx = 0;
		int aimIndex = judgeIndexTag(tag);
		for (Field field : fields) {
			doc.add(field);
		}
		int successIndex = 0;
		for (IndexWriter writer : writers) {
			if( idx==aimIndex ){
				try {
					writer.addDocument(doc);
					writer.optimize();
					successIndex++;
					System.out.println(tag+"索引添加成功"+writer.toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					try {
						this.rollback(successIndex);
					} catch (Exception ex) {
						throw ex;
					}
					throw new SearchException(tag+"索引添加失败!", e,
							SearchException.ErrorCode.LuceneError);
				}
			}
			else{
				System.out.println(tag+"未添加索引");
			}
			idx += 1;
		}
		return true;
	}

	private void rollback(int successIndex) throws SearchException {
		for (int i = 0; i <= successIndex; i++) {
			IndexWriter rWriter = writers.get(i);
			try {
				// 之前添加的进行回滚,(回滚会自动调用close方法)
				rWriter.rollback();
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				throw new SearchException("索引回滚失败!", ex,
						SearchException.ErrorCode.LuceneError);
			}
			try {
				rWriter = new IndexWriter(directorys.get(successIndex),
						analyzer, IndexWriter.MaxFieldLength.UNLIMITED);
				rWriter.setMaxBufferedDocs(500);
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				throw new SearchException("“索引写”对象构造失败!", ex,
						SearchException.ErrorCode.LuceneError);
			}
		}
	}

	protected boolean deleteIndex(String uri) throws Exception {
		int successIndex = 0;
		for (IndexWriter writer : writers) {
			try {
				writer.deleteDocuments(new Term(SearchBean.Field.URI, uri));
				writer.optimize();
				successIndex++;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				try {
					this.rollback(successIndex);
				} catch (Exception ex) {
					throw ex;
				}
				throw new SearchException("索引添加失败!", e,
						SearchException.ErrorCode.LuceneError);
			}
		}
		return true;
	}
	
	protected void commit(){
		for (IndexWriter writer : writers) {
			try {
				writer.commit();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.getLogger(this.getClass()).error(e.getMessage(), e);
			}
		}
	}
	

	public abstract boolean createIndex();


	protected abstract List<Field> getFileds(SearchBean bean) throws Exception;

}
