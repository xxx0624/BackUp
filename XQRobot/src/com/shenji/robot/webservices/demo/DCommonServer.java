package com.shenji.robot.webservices.demo;

import com.shenji.onto.reasoner.server.ReasonerTreeServer;
import com.shenji.robot.webservices.port.CommonServer;
import com.shenji.robot.webservices.port.OntoReasoning;
import com.shenji.robot.webservices.port.Search;

public class DCommonServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		boolean b=new Search().rebuildIndex();
		b=b&new OntoReasoning().reBuildFaqReasonerTree();
		System.out.println(b);
		
	}
	
	public static void dLogin(){
		CommonServer server=new CommonServer();
		System.out.println(server.Login("1", "1"));
	}

}
