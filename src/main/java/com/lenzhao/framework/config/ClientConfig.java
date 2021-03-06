package com.lenzhao.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *客户端配置参数,包括ip，端口，服务名，接口等信息
 */
public class ClientConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(ClientConfig.class);
	
	private int maxThreadCount = Runtime.getRuntime().availableProcessors();
	
	private int readTimeout = 0;
	
	private String tcpNoDelay = "true";
	
	private String reuseAddress = "true";
	
	private static ClientConfig clientConfig = new ClientConfig();
	
	private Map<String,ServiceConfig> services = null;
	
	private ClientConfig() {
		
	}
	
	public static ClientConfig getInstance() {
		return clientConfig;
	}
	
	public ServiceConfig getService(String name) {
		return services.get(name);
	}
	
	private volatile boolean isInit = false;
	//非静态代码块
	{
		if(!isInit) {
			this.root = getRoot();
			services= this.parseService();
			isInit = true;
		}
	}
	
	private Document document = null;
	
	private Element root = null;
	
	private Element getRoot() {
		Document doc = getDocument();
		List<Element> list = doc.selectNodes("//application");
		if (list.size() > 0) {
			Element aroot = list.get(0);
			return aroot;
		}
		return null;
	}
	
	private InputStream getFileStream(String fileName) {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
		return is;
	}
	
	private Document getDocument() {
		InputStream is = getFileStream("config.xml");
		try {
			if (document == null) {
				SAXReader sr = new SAXReader();
				sr.setValidation(false);
				if (is == null) {
					throw new RuntimeException("can not find config file...");
				}
				document = sr.read(is);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("get xml file failed");
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return document;
	}
	
	public Map<String,ServiceConfig> parseService() {

		Map<String,ServiceConfig> result = new HashMap<String, ServiceConfig>();
		List<Element> serviceList = root.selectNodes("//service");
		for (Element serviceNode : serviceList) {
			String name = serviceNode.attributeValue("name");// service name
			String connectStr = serviceNode.attributeValue("connectStr");
			String maxConnection = serviceNode.attributeValue("maxConnection");
			String async = serviceNode.attributeValue("async");

			if (name.equals("")) {
				logger.warn("configFile :a rpcservice's name is empty.");
				continue;
			}
			if (connectStr.equals("")) {
				logger.warn("configFile:rpcservice［" + name
						+ "］ has an empty interface configure.");
				continue;
			}
			ServiceConfig service = new ServiceConfig(name,connectStr,maxConnection,async);
			result.put(name, service);
		}
		return result;
	}

	public int getMaxThreadCount() {
		return maxThreadCount;
	}

	public void setMaxThreadCount(int maxThreadCount) {
		this.maxThreadCount = maxThreadCount;
	}

	public String getTcpNoDelay() {
		return tcpNoDelay;
	}

	public void setTcpNoDelay(String tcpNoDelay) {
		this.tcpNoDelay = tcpNoDelay;
	}

	public String getReuseAddress() {
		return reuseAddress;
	}

	public void setReuseAddress(String reuseAddress) {
		this.reuseAddress = reuseAddress;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}
	
}
