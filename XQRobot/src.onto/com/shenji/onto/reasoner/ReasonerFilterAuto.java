package com.shenji.onto.reasoner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.shenji.onto.mapping.FaqMapServices;
import com.shenji.onto.reasoner.data.ReasonerTree;
import com.shenji.onto.reasoner.data.SearchOntoBean;
import com.shenji.onto.reasoner.server.ReasonerTreeServer;
import com.shenji.robot.exception.OntoReasonerException;
import com.shenji.robot.exception.OntoReasonerException.ErrorCode;
import com.shenji.robot.log.HtmlLog;
import com.shenji.search.bean.XQSearchBean;

public class ReasonerFilterAuto extends ReasonerStrategy {
	public enum DoOneFaqResult {
		Continue, Break, Return;
	}

	public boolean doAfterReasoning(Object... obj) {
		/*
		 * List<String> reFaqList = (List<String>) obj[0]; if (reFaqList != null
		 * && reFaqList.size() > 0 && reFaqList.get(0) != null) {
		 * ReasonerComplex.ListCompara compara = new ListCompara();
		 * Collections.sort(reFaqList, compara); return true; } else return
		 * false;
		 */
		// 排序不做了
		return true;
	}

	@Override
	public List<SearchOntoBean> reasoning(Object... obj)
			throws OntoReasonerException {
		// TODO Auto-generated method stub
		List<XQSearchBean> beans = null;
		List<SearchOntoBean> reBeans = null;
		ReasonerTree[] userTrees = null;
		try {
			String sentence = (String) obj[0];
			beans = (List<XQSearchBean>) obj[1];
			reBeans = new ArrayList<SearchOntoBean>();
			if (beans == null || beans.size() == 0)
				return null;
			try{
			// 复合推理是没有交互，暂时没有加用户名
				userTrees = ReasonerTreeServer.getInstance().getUserReasonerTree(
					sentence);
			} catch (Exception e) {
				// TODO: handle exception
				throw new OntoReasonerException(e, ErrorCode.UnKnow);
			}
			// 用户推理树为空或者没有
			if (userTrees == null || userTrees.length == 0)
				// 推理树没有返回空
				return null;
			// 用户推理树只有THING节点，没有分出来类
			// Log.promptMsg("树高："+userTrees[0].getTreeHight());
			if (userTrees[0].getTreeHight() == 0) {
				throw new OntoReasonerException("UserTree is Null!",
						OntoReasonerException.ErrorCode.UserTreeIsNull);//没有定位
			}
			try {
				HtmlLog.info("getUserReasonerTree", new Object[] { sentence },
						userTrees[0].toXmlString("utf-8"), true);
				Set<String> filterSet = ReasonerTreeServer.getInstance()
						.filter(userTrees[0]);

				for (XQSearchBean bean : beans) {
					// 这里可能有点问题
					if (filterSet.contains(FaqMapServices
							.analysisHrefTogetName(bean.getUri()))) {
						SearchOntoBean ontoBean = new SearchOntoBean(bean);
						ontoBean.setOntoDimension(1);// 这里默认都先设置为1
						reBeans.add(ontoBean);
					}
				}
				doAfterReasoning(new Object[] { reBeans });
				return reBeans;
			} catch (Exception e) {
				// TODO: handle exception
				throw new OntoReasonerException(e, ErrorCode.UnKnow);
			}
			/*
			 * if (reFaqList != null && reFaqList.size() != 0) { String[] reStrs
			 * = reFaqList .toArray(new String[reFaqList.size()]);
			 * 
			 * for(String str:reStrs){ Log.debugSystemOut(str); }
			 * 
			 * // return reStrs; return null; } else return null;
			 */
		} finally {
			// 用户树要清除，占内存
			if (userTrees != null && userTrees.length != 0) {
				for (ReasonerTree tree : userTrees) {
					tree.clear();
				}
			}
			

		}

	}
}
