//ログイン画面に遷移するためのアクションクラス
package com.internousdev.pumpkin.action;

import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionSupport;

public class GoLoginAction extends ActionSupport implements SessionAware{

	private String cartFlag;
	private Map<String, Object> session;

	public String execute(){
		if(cartFlag != null){
			session.put("cartFlag", cartFlag);
		}

		return SUCCESS;
	}

	public String getCartFlag(){
		return cartFlag;
	}

	public void setCartFlag(String cartFlag){
		this.cartFlag = cartFlag;
	}

	public Map<String, Object> getSession(){
		return this.session;
	}

	@Override
	public void setSession(Map<String, Object> session){
		this.session = session;
	}

}