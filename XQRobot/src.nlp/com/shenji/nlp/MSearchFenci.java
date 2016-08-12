package com.shenji.nlp;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;

import com.shenji.nlp.inter.IChineseWordSegmentation;
import com.shenji.robot.inter.IComFenciServer;
import com.shenji.search.control.FenciControl;

/**
 * 中文分词
 * @author zhq
 *
 */
public class MSearchFenci implements IChineseWordSegmentation{
	private IComFenciServer iFenciServer;
	public MSearchFenci(IComFenciServer iFenciServer){	
		this.iFenciServer=iFenciServer;
	}
	
	
	@Override
	public String[] segment(String sentence) throws RemoteException{
		// TODO Auto-generated method stub		
		return iFenciServer.iKAnalysis(sentence).split("/");
	}
	

}
