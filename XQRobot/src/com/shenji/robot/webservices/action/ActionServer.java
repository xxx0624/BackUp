package com.shenji.robot.webservices.action;

import com.shenji.common.log.Log;
import com.shenji.robot.action.DBUserManager;
import com.shenji.robot.util.StringUtil;

public class ActionServer {
	public int login(String userName, String passWord) {
		DBUserManager dbUserManager = null;
		int reFlag = -2;
		try {
			dbUserManager = new DBUserManager();
			reFlag = dbUserManager.login(userName, passWord);
		} catch (Exception e) {
			Log.getLogger(this.getClass()).error(e.getMessage(),e);
		} finally {
			if (dbUserManager != null)
				dbUserManager.close();
		}
		return reFlag;

	}
}
