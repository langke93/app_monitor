/**
 * 
 */
package org.langke.jetty.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;


public class HttpClientUtil {
	private static final Logger log = LoggerFactory.getLogger(HttpClientUtil.class);
	private static HttpClient client = null;
	private static MultiThreadedHttpConnectionManager conn_manager = null;
	private static HttpConnectionManagerParams cmanager_params = null;
	static{
		if(client==null) init();
	}
	private static void init(){
		if (conn_manager == null)
			conn_manager = new MultiThreadedHttpConnectionManager();
		if (cmanager_params == null)
			cmanager_params = new HttpConnectionManagerParams();
		// config the HTTP client visit performance.
		cmanager_params.setDefaultMaxConnectionsPerHost(1000);
		cmanager_params.setMaxTotalConnections(1024);
		cmanager_params.setConnectionTimeout(300000);
		conn_manager.setParams(cmanager_params);
		if (client == null)
			client = new HttpClient(conn_manager);
	}
	
	public static StrIntBag executeGet(String url){
		return executeGet(url, null);
	}
	
	public static StrIntBag executeGet(String url,String host){
		GetMethod get = null;
		StrIntBag bag = new StrIntBag();
		try {
			get = new GetMethod(url);
			get.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"UTF-8");
			get.getParams().setParameter(HttpMethodParams.USE_EXPECT_CONTINUE, false);
			if(host!=null)
				get.getParams().setParameter(HttpMethodParams.VIRTUAL_HOST, host);
			int status = client.executeMethod(get);
			String resp = null;
			if(get.getResponseContentLength()!=-1)
				resp = get.getResponseBodyAsString();
			else
				resp = getStreamToString(get.getResponseBodyAsStream());
			bag._str = resp;
			bag._int = status;
			return bag;
		} catch (Exception e) {
			log.error("{}", e);
		} finally{
			if(get != null){
				get.releaseConnection();
			}
		}
		return null;
	}
	public static StrIntBag tryExecuteGet(String url){
		StrIntBag bag = executeGet(url);
		if(bag != null){
			return bag;
		}
		int tryCount = 3;
		while(bag == null && (tryCount--) > 0 ){
			bag = executeGet(url);
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				log.error("{}",e);
			}
		}
		return bag;
	}
	
	/**
	 * ald 转url post使用
	 * @param uri
	 * @param requestBody
	 * @return
	 */
	public static StrIntBag executeForAld(String uri, String requestBody){
		PostMethod post = null;
		StrIntBag bag = new StrIntBag();
		try {
			post = new PostMethod(uri);
			post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"UTF-8");
			post.getParams().setParameter(HttpMethodParams.USE_EXPECT_CONTINUE, false);
			if(requestBody != null){
				RequestEntity reqEntity = new StringRequestEntity(requestBody,"application/json","UTF-8");
				post.setRequestEntity(reqEntity);
			}
			int status = client.executeMethod(post);
			String resp = getStreamToString(post.getResponseBodyAsStream()); 
			bag._str = resp;
			bag._int = status;
			return bag;
		} catch (Exception e) {
			log.error("{}",e);
		} finally{
			if(post != null){
				post.releaseConnection();
			}
		}
		return null;
	}
	
	
	
	public static StrIntBag execute(String uri, String requestBody){
		PostMethod post = null;
		StrIntBag bag = new StrIntBag();
		try {
			post = new PostMethod(uri);
			post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"UTF-8");
			post.getParams().setParameter(HttpMethodParams.USE_EXPECT_CONTINUE, false);
			if(requestBody != null){
				RequestEntity reqEntity = new StringRequestEntity(requestBody,"application/json","UTF-8");
				post.setRequestEntity(reqEntity);
			}
			int status = client.executeMethod(post);
			String resp = post.getResponseBodyAsString();
			bag._str = resp;
			bag._int = status;
			return bag;
		} catch (Exception e) {
			log.error("{},{}",uri,requestBody);
			log.error("",e);
		} finally{
			if(post != null){
				post.releaseConnection();
			}
		}
		return null;
	}
	
	/**
	 * 失败后，重试3次
	 * @param uri
	 * @param requestBody
	 * @return
	 */
	public static StrIntBag tryExecute(String uri, String requestBody){
		StrIntBag bag = execute(uri, requestBody);
		if(bag != null){
			return bag;
		}
		int tryCount = 3;
		while(bag == null && (tryCount--) > 0 ){
			bag = execute(uri, requestBody);
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				log.error("{}", e);
			}
		}
		return bag;
	}
	

	public static JSONObject callUrlToJSON(String url,String json){
		StrIntBag bag = null;
		if(log.isInfoEnabled()){
			String key = "password";
			String key2 = "newPassword";
			String body = json;
			if(body!=null && body.indexOf(key)!=-1){
				JSONObject bodyObj= JSONObject.parseObject(body);
				if(bodyObj.containsKey(key)){
					bodyObj.put(key, "***");
				}
				body = bodyObj.toJSONString();
				bodyObj = null;
			}
			if(body!=null && body.indexOf(key2)!=-1){
				JSONObject bodyObj= JSONObject.parseObject(body);
				if(bodyObj.containsKey(key2)){
					bodyObj.put(key2, "***");
				}
				body = bodyObj.toJSONString();
				bodyObj = null;
			}
			log.info("{} {}",url,body);
		}
		try {
			CostTime cost = new CostTime();
			cost.start();
			bag = HttpClientUtil.execute(url,json);
			if(log.isWarnEnabled() && cost.cost()>300){
				log.warn("{} costTime:{}", url,cost.cost());
			}
		} catch (Exception e) {
			log.error("{},{},{}", url+json);
		}
		if(bag == null || bag._int != 200){
			log.warn("{},{}", url, bag);
			return null;
		}
		String respBody = bag._str;
		JSONObject jsonObj = null ;
		try{
			jsonObj = JSONObject.parseObject(respBody);
		}catch(Exception e){
			log.error("url:{},parm:{}",url,json);
			log.error("resp:{}",respBody);
		}
		//if(log.isInfoEnabled())
		//	log.info("{} {}",url,jsonObj);
		return jsonObj;
	}
	
	public static String getStreamToString(InputStream resStream) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(resStream));
		StringBuffer resBuffer = new StringBuffer();
		String resTemp = "";
		while((resTemp = br.readLine()) != null){
			resBuffer.append(resTemp);
		}
		String resp = resBuffer.toString(); 
		br.close();
		resStream.close();
		return resp;
	}
	
	public static void main(String[] args) {
		StrIntBag resp = execute("http://10.10.10.201:9200/imggather/_search","{\"query\":\"时尚\"}");
		System.out.println(resp._str);
	}
}
