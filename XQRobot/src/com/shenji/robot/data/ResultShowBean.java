package com.shenji.robot.data;

import java.util.List;

import com.shenji.search.control.IEnumSearch.ResultCode;


public class ResultShowBean {
	private ResultCode code;
	private List<String> result;

	public ResultShowBean(ResultCode code, List<String> result) {
		this.code = code;
		this.result = result;
	}

	public ResultCode getCode() {
		return code;
	}

	public void setCode(ResultCode code) {
		this.code = code;
	}

	public List<String> getResult() {
		return result;
	}

	public void setResult(List<String> result) {
		this.result = result;
	}

}
