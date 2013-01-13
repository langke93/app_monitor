package org.langke.jetty.common;

import java.util.Calendar;

public class TableUtil {
	public final static String yyyymmdd = DateUtil.format(Calendar.getInstance().getTime(),"yyyy_MM_dd");
	public final static String table_name_pre = "T_API_MONITOR_";
	public final static String table_name = table_name_pre+yyyymmdd;
}
