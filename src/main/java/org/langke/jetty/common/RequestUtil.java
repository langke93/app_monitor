package org.langke.jetty.common;

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

public class RequestUtil {

	public static String getRequestString(HttpServletRequest request) throws IOException{
		String str="";
		if(request.getInputStream().available()==0){
			for(Object obj:request.getParameterMap().keySet()){
				str += obj.toString();
			}
		}else{
			BufferedInputStream bis=new BufferedInputStream(request.getInputStream());
			try{
				byte read[] = new byte[200*1024]; 
				while(( bis.read(read)) != -1 ){ 
					str+=new String(read);
				}
			}finally{
				bis.close();
			}
		}
		return str.trim();
	}
}
