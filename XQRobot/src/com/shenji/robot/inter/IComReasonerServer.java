package com.shenji.robot.inter;

import java.util.List;

import com.shenji.onto.reasoner.data.SearchOntoBean;
import com.shenji.robot.exception.OntoReasonerException;

public interface IComReasonerServer {
	public List<SearchOntoBean> reasoning(Object... obj)
			throws OntoReasonerException;
}
