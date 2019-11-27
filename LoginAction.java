//ログイン画面から他のページへ遷移するためのクラス
package com.internousdev.pumpkin.action;

import java.util.List;
import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;

import com.internousdev.pumpkin.dao.CartInfoDAO;
import com.internousdev.pumpkin.dao.UserInfoDAO;
import com.internousdev.pumpkin.dto.CartInfoDTO;
import com.internousdev.pumpkin.util.InputChecker;
import com.opensymphony.xwork2.ActionSupport;

public class LoginAction extends ActionSupport implements SessionAware{

	private Map<String, Object> session;
	private String userId;
	private String password;
	private boolean savedUserIdFlag;
	private long totalPrice;
	private String isNotUserInfoMessage;

	private List<String> userIdErrorMessageList;
	private List<String> passwordErrorMessageList;
	private List<CartInfoDTO> cartInfoDTOList;

	public String execute(){

		String result = ERROR;//login.jspへ遷移
		UserInfoDAO userInfoDAO = new UserInfoDAO();
		session.remove("savedUserIdFlag");

		//ユーザー情報入力完了画面から遷移
		if(session.containsKey("createUserFlag")){
			userId = session.get("userIdForCreateUser").toString();

			//ユーザー情報入力完了画面から遷移する際にsessionに残っているため削除する
			session.remove("userIdForCreateUser");
			session.remove("password");
			session.remove("createUserFlag");

		}else{
			InputChecker ic = new InputChecker();
			userIdErrorMessageList = ic.doCheck("ユーザーID", userId, 1, 8, true, false, false, true, false, false);
			passwordErrorMessageList = ic.doCheck("パスワード", password, 1, 16, true, false, false, true, false, false);

			if(userIdErrorMessageList.size() > 0 || passwordErrorMessageList.size() > 0){
				session.put("logined", 0);
				return result;
			}

			if(!userInfoDAO.isExistsUserInfo(userId, password)){
				isNotUserInfoMessage = "ユーザーIDまたはパスワードが異なります。";
				return result;
			}
		}

		if(!session.containsKey("tempUserId")){
			return "sessionTimeout";//sessionError.jspへ遷移
		}

		CartInfoDAO cartInfoDAO = new CartInfoDAO();
		//カート情報の紐付け(カート画面から決済をする際にログイン作業が必要なため使用)
		String tempUserId = session.get("tempUserId").toString();
		List<CartInfoDTO> cartInfoDTOListForTempUser = cartInfoDAO.getCartInfoDTOList(tempUserId);
		if(cartInfoDTOListForTempUser != null && cartInfoDTOListForTempUser.size() > 0){
			boolean cartresult = changeCartInfo(cartInfoDTOListForTempUser, tempUserId);
				if(!cartresult){
					return "DBError";//systemError.jspへ遷移
				}
		}

		//ユーザー情報をsessionに登録し、tempUserIdを削除する
		session.put("userId", userId);
		session.put("logined", 1);
		if(savedUserIdFlag){
			session.put("savedUserIdFlag", true);
		}

		session.remove("tempUserId");

		//カートフラグありか無しかで次の遷移先を決める
		if(session.containsKey("cartFlag")){
			session.remove("cartFlag");
			cartInfoDTOList = cartInfoDAO.getCartInfoDTOList(userId);
			totalPrice = cartInfoDAO.getTotalPrice(userId);
			result = "cart";//cart.jspへ遷移

		}else{
			result = SUCCESS;//home.jspへ遷移
		}
		return result;
	}

	/**
	 * DBにデータを更新・作成する
	 * @param cartInfoDTOListForTempUser 仮ユーザーIDに紐づくカート情報
	 * @param tempUserId 仮ユーザーID
	 */
	private boolean changeCartInfo(List<CartInfoDTO> cartInfoDTOListForTempUser, String tempUserId){
		int count = 0;
		CartInfoDAO cartInfoDAO = new CartInfoDAO();
		boolean result = false;

		for(CartInfoDTO dto : cartInfoDTOListForTempUser){
			/** 処理対象のカート情報とDBのカート情報テーブルにユーザーIDに紐づく同じ商品IDのカート情報が存在するかチェックする。
			 * tempUserIdとuserIdの情報が一致している場合はtempUserIdのカート情報をuserIdのカート情報に足した後tempUserIdを削除
			 * 存在しない場合はtempUserIdとuserIdの情報が一致していない場合は、tempUserIdとuserIdの情報をuserIdに書き換える。
			 * for文の最初の文に戻り、再度処理。
			 */
			if(cartInfoDAO.isExistsCartInfo(userId, dto.getProductId())){
				count += cartInfoDAO.updateProductCount(userId, dto.getProductId(), dto.getProductCount());
				cartInfoDAO.CartDelete(String.valueOf(dto.getProductId()), tempUserId);
			}else{
				count += cartInfoDAO.linkToUserId(tempUserId,userId, dto.getProductId());
			}
		}
		if(count == cartInfoDTOListForTempUser.size()){
			result = true;
		}
		return result;
	}

	public String getUserId(){
		return userId;
	}

	public void setUserId(String userId){
		this.userId = userId;
	}

	public String getPassword(){
		return password;
	}

	public void setPassword(String password){
		this.password = password;
	}

	public boolean isSavedUserIdFlag(){
		return savedUserIdFlag;
	}

	public void setSavedUserIdFlag(boolean savedUserIdFlag){
		this.savedUserIdFlag = savedUserIdFlag;
	}

	public List<String> getUserIdErrorMessageList(){
		return userIdErrorMessageList;
	}

	public void setUserIdErrorMessageList(List<String> userIdErrorMessageList){
		this.userIdErrorMessageList = userIdErrorMessageList;
	}

	public List<String> getPasswordErrorMessageList(){
		return passwordErrorMessageList;
	}

	public void setPasswordErrorMessageList(List<String> passwordErrorMessageList){
		this.passwordErrorMessageList = passwordErrorMessageList;
	}

	public String getIsNotUserInfoMessage(){
		return isNotUserInfoMessage;
	}

	public void setIsNotUserInfoMessage(String isNotUserInfoMessage){
		this.isNotUserInfoMessage = isNotUserInfoMessage;
	}

	public long getTotalPrice(){
		return totalPrice;
	}

	public void setTotalPrice(int totalPrice){
		this.totalPrice = totalPrice;
	}

	public List<CartInfoDTO> getCartInfoDTOList(){
		return cartInfoDTOList;
	}

	public void setCartInfoDTOList(List<CartInfoDTO> cartInfoDTOList){
		this.cartInfoDTOList = cartInfoDTOList;
	}

	public Map<String, Object> getSession(){
		return this.session;
	}

	@Override
	public void setSession(Map<String, Object> session){
		this.session = session;
	}

}
