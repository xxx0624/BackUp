package com.shenji.robot.webservices.port.test;

import com.shenji.robot.webservices.port.OntoEdit;

public class DPort {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		OntoEdit edit=new OntoEdit();
		String token =edit.openOntologyFromDB("KnowLedge", "1");
		System.out.println(token);
		System.out.println(edit.getBasicInformation(token));
	}

}
