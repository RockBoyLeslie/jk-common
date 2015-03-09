package com.jk.common.gateway.http;

import com.jk.common.gateway.http.impl.CommunicationServiceImpl;
import org.apache.http.client.ClientProtocolException;

public class CommunicationServiceTest {
	public static void main(String[] args) {
	    CommunicationServiceImpl service = new CommunicationServiceImpl();
		service.init();
		
		try {
			String result = service.executeRequest(HttpMethodEnum.GET, "http://localhost:8060", null, null);
			System.out.println(result);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
