package org.langke.jetty.common;

import java.util.Calendar;

public class TableUtil {
	public final static String yyyymmdd = getDateFormat();
	public final static String T_API_MONITOR_PRE = "T_API_MONITOR_";
	public final static String T_MENU = "T_MENU";
	public static String getDateFormat(){
		return DateUtil.format(Calendar.getInstance().getTime(),"yyyy_MM_dd");
	}
	public static String getT_API_MONITOR_YYYYMMDD(){
		return T_API_MONITOR_PRE+getDateFormat();
	}
}
