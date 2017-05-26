package com.shenji.search.enums;

public class LogType {
	public static enum LogTypeEnum{
		Log_local_file(1),//打印小琼问答最多top3到本地
		Not_log(2);
		
		private int value;
		
		private LogTypeEnum(int value){
			this.value = value;
		}
		
		public int value(){
			return value;
		}
		
		public static LogTypeEnum valueOf(int value){
			for (LogTypeEnum t : LogTypeEnum.values()) {
				if (t.value() == value) {
					return t;
				}
			}
			return null;
		}
	}
}
