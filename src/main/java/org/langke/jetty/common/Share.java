package org.langke.jetty.common;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.util.TypeUtils;

public class Share {

	
	/**
	 * 返回字符信息到客户端 ajax 使用
	 * @param content
	 * @param response
	 */
	public static void printWriter(Object content,HttpServletResponse response){
		PrintWriter writer = null;
		try{
			writer = response.getWriter();
			writer.print(content);
			
		}
		catch(Exception e){
			e.printStackTrace();
		}finally{
			if(writer!=null){
				writer.flush();
				writer.close();
			}
		}
	}
	
	/**
	 * 返回字符信息到客户端  iframe使用
	 * @param content 内容
	 * @param function 父框架方法
	 * @param response
	 */
	public static void printWriter(Object content,String function,HttpServletResponse response){
		PrintWriter writer = null;
		try{
			content = "<script>window.parent."+function+"('"+content+"')</script>";
			writer = response.getWriter();
			writer.print(content);
			
		}
		catch(Exception e){
			e.printStackTrace();
		}finally{
			if(writer!=null){
				writer.flush();
				writer.close();
			}
		}
	}
	
	/**
	 * 封装请求参数
	 * 
	 * @param request
	 * @param c
	 *           封装类
	 * @return c对象
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static Object requestBean(HttpServletRequest request, Class<?> c) {
		Object o = null;
		try {
			o = c.newInstance();
			Field[] fieldlist = c.getDeclaredFields();
			for (int i = 0; i < fieldlist.length; i++) {
				Field field = fieldlist[i];
				field.setAccessible(true);
				String value = request.getParameter(field.getName());
				if (value != null && !value.equals("")) {
					try {
						try{
							value = java.net.URLDecoder.decode(value, "UTF-8");
						}catch (Exception e) {
						}
						value = value.replace("%26","&");
						field.set(o, TypeUtils.castToJavaBean(value.trim(), field.getType()));
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return o;
	}

	/**
	 * 封装请求参数
	 * 
	 * @param request
	 * @param c
	 *           封装类
	 * @param decode
	 *           是否进行utf-8编码
	 * @return c对象
	 * @throws UnsupportedEncodingException
	 */
	public static Object requestBean(HttpServletRequest request, Class<?> c, boolean decode) {
		Object o = null;
		try {
			o = c.newInstance();
			Field[] fieldlist = c.getDeclaredFields();
			for (int i = 0; i < fieldlist.length; i++) {
				Field field = fieldlist[i];
				field.setAccessible(true);
				String value = request.getParameter(field.getName());
				if (value != null && !value.equals("")) {
					try {
						if (decode) {
							try{
								value = java.net.URLDecoder.decode(value, "UTF-8");
							}catch (Exception e) {
							}
						}
						field.set(o, TypeUtils.castToJavaBean(value.trim(), field.getType()));
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return o;
	}

	/**
	 * 反编码对象内的String属性 utf-8
	 * 
	 * @param obj
	 * @return
	 */
	public static Object ojbectDecoder(Object obj) {
		try {
			if (obj == null) {
				return null;
			}
			Field[] fields = obj.getClass().getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				field.setAccessible(true);
				Type type = field.getGenericType();
				if (!type.equals(String.class))
					continue;
				Object value = field.get(obj);
				if (value != null && !value.toString().equals("")) {
					field.set(obj, URLDecoder.decode(value.toString(), "UTF-8"));
				}
			}
			return obj;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 取客户端IP地址，传说可取到通过代理的IP
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpServletRequest request) {
	    String ip = request.getHeader("x-forwarded-for");      
	    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
	        ip = request.getHeader("Proxy-Client-IP");      
	    }      
	    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
	        ip = request.getHeader("WL-Proxy-Client-IP");      
	    }      
	    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
	        ip = request.getRemoteAddr();      
	    }      
	    return ip;      
	}
 
	/**
	 * 取用户权限对象
	 * @param request
	 * @return
	 */
	public static List<?> getAclObject(HttpServletRequest request){
		List<?> list = (List<?>) request.getSession().getAttribute("aclObject");
		return list;
	}
	
	public static boolean isNumeric(String str){
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if( !isNum.matches() ){
			return false;
		}
		return true;
	} 
	
	/**
	 * 取参数
	 * @param request
	 * @return
	 */
	public String getQueryString(HttpServletRequest request){
		String str = request.getQueryString();
		if(str==null)
			str="";
		else
			str = "?"+str;
		return str;
	}

    public static String htmlFilter(String inputString) {
   	 if (inputString==null) return null;
   	  String htmlStr = inputString; // 含html标签的字符串
   	  String textStr = "";
   	  java.util.regex.Pattern p_script;
   	  java.util.regex.Matcher m_script;
   	  java.util.regex.Pattern p_style;
   	  java.util.regex.Matcher m_style;
   	  java.util.regex.Pattern p_html;
   	  java.util.regex.Matcher m_html;
   	  try {
   	   String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>"; // 定义script的正则表达式{或<script>]*?>[\s\S]*?<\/script>
   	   // }
   	   String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>"; // 定义style的正则表达式{或<style>]*?>[\s\S]*?<\/style>
   	   // }
   	   String regEx_html = "<a[^>]+>|</a>"; // 定义HTML标签的正则表达式

   	   p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
   	   m_script = p_script.matcher(htmlStr);
   	   htmlStr = m_script.replaceAll(""); // 过滤script标签

   	   p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
   	   m_style = p_style.matcher(htmlStr);
   	   htmlStr = m_style.replaceAll(""); // 过滤style标签

   	   p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
   	   m_html = p_html.matcher(htmlStr);
   	   htmlStr = m_html.replaceAll(""); // 过滤html标签

   	   textStr = htmlStr;

   	  } catch (Exception e) {
   		  e.printStackTrace();
   	  }

   	  return textStr;
   	}
    
    
    /**
	 * 将对象转字符串
	 * 
	 * @param obj
	 * @return
	 */
	public static String objectToString(Object parameter) {
		try {
			if (parameter == null) {
				return "";
			}
			StringBuffer str = new StringBuffer();
			final String split = "&";
			final String equal = "=";

			// 处理Map对象
			if (parameter instanceof Map) {
				Map<?, ?> p = (Map<?, ?>) parameter;
				Iterator<?> iterator = p.keySet().iterator();
				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					Object obj = p.get(key);
					if (obj instanceof String) {
						str.append(split + key + equal + obj.toString());
					}
				}
			}
			else {
				// 处理bean对象
				Field[] fields = parameter.getClass().getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
					Field field = fields[i];
					field.setAccessible(true);
					Type type = field.getGenericType();
					if (type.equals(String.class)) {
						String name = field.getName();
						Object value = field.get(parameter);
						if (value != null && !value.toString().equals("")) {
							str.append(split + name + equal + value.toString());
						}
					}
				}
			}
			String p = str.toString();
			if (!p.equals("")) {
				p = p.substring(1);
			}
			return p;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	
	public static boolean HttpURLConnection(String path,Object b){
		URL url = null;
	    java.net.HttpURLConnection httpurlconnection = null;
		try {
		  url = new URL(path);
	      httpurlconnection = (java.net.HttpURLConnection) url.openConnection();
	      httpurlconnection.setDoOutput(true);
	      httpurlconnection.setRequestMethod("POST");
	      String username=objectToString(b);
	      System.out.println("输出参数："+path+"?"+username);
	      httpurlconnection.getOutputStream().write(username.getBytes());
	      httpurlconnection.getOutputStream().flush();
	      httpurlconnection.getOutputStream().close();
	      InputStream in = httpurlconnection.getInputStream();
	      int read=0;
          byte[] byteBuf=new byte[9412];
          StringBuffer str = new StringBuffer();
          while((read=in.read(byteBuf))!=-1){
        	  str.append(new String(byteBuf, 0, read));
          }
          in.close();
          System.out.println("返回值："+str.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
		     if(httpurlconnection!=null)
		        httpurlconnection.disconnect();
		}

		return false;
	}
}
