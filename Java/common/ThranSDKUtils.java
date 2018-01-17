package com.og.common;

import java.util.HashMap;
import java.util.Map;

import org.cocos2dx.lib.Cocos2dxGLSurfaceView;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.excelliance.kxqp.sdk.GameSdk;
import com.excelliance.kxqp.sdk.IQueryUpdateCallback;
import com.og.danjiddz.LandlordDJ;
import com.og.danjiddz.R;
//import com.og.gameconfig.OGSdkPlatform;
import com.og.gameconfig.OGLoadParamsCallBack;
import com.og.unite.charge.OGSdkIPayCenter;
import com.og.unite.data.OGSdkUser;
import com.og.unite.login.OGSdkIUCenter;
//import com.og.unite.login.OGSdkPlatform;
import com.og.unite.main.OGIDispatcherCallback;
import com.og.unite.main.OGSdkCallback;
import com.og.unite.main.OGSdkPlatform;
import com.og.unite.serverInfo.OGSdkServerInfo;
//import com.og.unite.shop.OGSDKShopCenter;
import com.og.unite.shop.OGSdkIShopCenter;
import com.og.unite.PayDetailList.OGSdkPayDetailList;
//import com.og.unite.PayDetailList.OGSdkPayDetailListCenter;
import com.og.unite.DisplayName.OGSdkDisplayName;
//import com.og.unite.DisplayName.OGSdkDisplayNameCenter;
import com.og.unite.shop.bean.OGSDKShopData;
import com.og.unite.main.OGSdkPlatform;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.JsonReader;
import android.util.Log;


public class ThranSDKUtils {
	private final static String LOGTAG = "thransdk";
	private static final String Rolename = "roleName";
	private static final String Certs = "token";
	private static final String SecertKey = "SecertKey";
	private static final String LoginType = "LoginType";
	private static final String Uid = "Uid";
	private static final String Error = "Error";
	private static final String SessionID = "SessionID";
	private static String GetGameParamTag = "getGameParam";
	private static final String DownLoadTag = "sdk download res log";
	private static final String LocalPushTag = "push event";
	private static final String ThranLoginTag = "thranlogin";

	public static int PushEventId = 0;
	static private Map<Integer, String> mapLoginError = new HashMap<Integer, String>(){
		{  
			put(0,"成功");
			put(1,"参数非法");
			put(2,"appid对应的gameid不存在");
			put(3,"证书获取失败");
			put(4,"服务器错误");
			put(5,"密码错误");
			put(6,"用户ID不存在");
			put(7,"新密码为空");
			put(8,"参数错误");
			put(20,"未知");
			put(21,"取消登录");
			put(22,"用户名密码错误为空");
			put(23,"不支持这种登录方式");
			put(24,"传递用户参数是null");
			put(25,"服务器应答错误");
			put(27,"无法识别服务器返回信息");
			put(28,"登录超时");
			put(29,"上次登录操作还没完成");
			put(1000,"未知");
			put(1001,"无网络");
			put(1002,"目标地址不合法");
			put(1003,"服务器回应错误");
			put(1004,"服务器没有响应");
			put(1005,"访问接口传递的参数不合法");			
			
		}
	};
	public static String getLoginErrorJson(int nCode)
	{
		String sJson;
		Map<String, Object> map = new HashMap<String, Object>();
		if (mapLoginError.containsKey(nCode))
		{
			map.put(Error, mapLoginError.get(nCode));
		}
		else
		{
			map.put(Error, "登录失败请重试");
		}
		JSONArray jmap = new JSONArray();  
        jmap.put(map);
		
	    sJson = jmap.toString();
	    //sJson = //.toJSONString(map);
//		Log.i("thransdk", "json error" + sJson);
		return sJson;
	}
	
	static private Map<Integer, String> mapRegistError = new HashMap<Integer, String>(){
		{
			put(2,"联众账号已存在");
			put(3,"联众账号格式非法");
			//put(4,"该角色名/显示名已被其他用户使用");
			//put(5,"角色名/显示名非法");
			//put(6,"创建角色失败");
		}
	};
	private static String getRegistErrorJson(int nCode)
	{
		String sJson;
		Map<String, Object> map = new HashMap<String, Object>();
		if (mapRegistError.containsKey(nCode))
		{
			map.put(Error, mapRegistError.get(nCode));
		}
		else
		{
			map.put(Error, "创建失败请重试");
		}
		JSONArray jmap = new JSONArray();  
        jmap.put(map);
		
	    sJson = jmap.toString();
		//sJson = JSON.toJSONString(map);
//		Log.i("thransdk", "json error" + sJson);
		return sJson;
	}
	
	// 登录回调
	public static native void onLoginCallback(int result, String data);	
	
	// 注册回调
	public static native void onRegistCallback(int result, String data);	
	public static native void onGetURLFromSDK(String data);	
	// 修改昵称回调
	private static native void onModifyNameCallback(int result, String data);
	private static native void nativeGetModifyName(String data);
	
	// 在线配置参数回调
	public static native void nativeCommonGameParamByKey(String key, String msg);

	// 在线下载资源
	public static native void nativeResourseDownloadFinished(String sMsg);
	public static native void nativeResourseDownloadProcessing(String sMsg);
	
	// 获取特殊商品信息
	public static native void onGetSpecialProductInfo(int result, String info);
	
	public static void bindPhone() {
		OGMainActivity.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				OGSdkPlatform.showPhoneBindView(OGMainActivity.getInstance());
			}
		});
	}
	
	public static String getThranSdkAppID() {
		String appid = "";
		try {
			String packageName = OGMainActivity.getInstance().getPackageName();
			PackageManager mgr = OGMainActivity.getInstance().getPackageManager();
			ApplicationInfo info = mgr.getApplicationInfo(packageName,
					PackageManager.GET_META_DATA);
			Object value = info.metaData.get("OG_APPID");
			if (value != null) {
				appid = value.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return appid;
	}
	
	public static String getThranSdkVersion() {
		String sdkVersion = "";
		try {
			sdkVersion = OGSdkPlatform.getSdkVersion();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sdkVersion;
	}
	
	public static String getUniqueID() {
		String uniqueID = "";
		try {
			uniqueID = OGSdkPlatform.getUniqueID();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return uniqueID;
	}
	
	public static void getSpecialProductInfo(String rolename) {
		Log.v("getSpecialProductInfo", "getSpecialProductInfo java start");
		try {
			OGSdkPlatform.getSpecialProductInfo(OGMainActivity.getInstance(), "baoyue", rolename, new OGSdkCallback() {
				
				@Override
				public void onFinished(String arg0) {
					// TODO Auto-generated method stub
					final String limitStr = arg0;
					Log.v("getSpecialProductInfo", "getSpecialProductInfo java limitStr =" + limitStr);
					Cocos2dxGLSurfaceView.getInstance().queueEvent(new Runnable() {
						@Override
						public void run() {
							Log.v("getSpecialProductInfo", "getSpecialProductInfo java run");
							onGetSpecialProductInfo(0, limitStr);
						}
					});
					
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	// 自动登录
		public static void autoLogin() {
			Log.d(ThranLoginTag, "thran sdk auto login start!");
			OGMainActivity.getInstance().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					OGSdkUser user = OGSdkUser.getInstance();
					OGSdkPlatform.loginPlatform(OGMainActivity.getInstance(), user, -1, new OGSdkIUCenter() {
								@Override
								public void onSuccess(final OGSdkUser user) {
								 Log.d(ThranLoginTag, "thran sdk auto login success role =" + user.getRolename());
									randomNickName("25010");
									Cocos2dxGLSurfaceView.getInstance().queueEvent(new Runnable() {
												@Override
												public void run() {
													onLoginCallback(0, user.getMsg());
												}
											});
								}

								@Override
								public void onError(final int code) {
								 Log.d(ThranLoginTag, "thran sdk auto login failed errorcode = " + code);
									Cocos2dxGLSurfaceView.getInstance().queueEvent(new Runnable() {
												@Override
												public void run() {
													onLoginCallback(code, getLoginErrorJson(code));
												}
											});
								}
							});
				}
			});		
		}
		
	public static void showAccountDialog() {
		Log.d(ThranLoginTag, "thran sdk showH5 login start!");
		OGMainActivity.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				OGSdkPlatform.showH5LogInView(OGMainActivity.getInstance());
			}
		});
	}	
	
	public static void onAccountDialogResult(int requestCode, final int resultCode, Intent data)
	{
		try {
			if (data == null){
				Cocos2dxGLSurfaceView.getInstance().queueEvent(new Runnable() {
					@Override
					public void run() {
						onLoginCallback(-1, "");
					}
				});
				return;
			}
			final String result = data.getStringExtra("result");
			Cocos2dxGLSurfaceView.getInstance().queueEvent(new Runnable() {
				@Override
				public void run() {
					if (resultCode==200) {
						onLoginCallback(0, result);
					}
					else if (resultCode == 100) {
						onLoginCallback(0, result);
					}
					else if (resultCode == 300) {
						onRegistCallback(0, result);
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
	}
	
	public static void thirdLogin(String channel) {
		final String str = channel;
		Log.d(ThranLoginTag,"thran sdk LoginThird channel = "+channel);
		OGMainActivity.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					OGSdkPlatform.login(OGMainActivity.getInstance(), str, new OGSdkIUCenter() {
						@Override
						public void onError(int arg0) {
							// TODO Auto-generated method stub
							final int errorCode = arg0;
							Log.d(ThranLoginTag,"thran sdk LoginThird failed errorcode = "+errorCode);
							Cocos2dxGLSurfaceView.getInstance().queueEvent(new Runnable() {
								@Override
								public void run() {
									onLoginCallback(errorCode, ThranSDKUtils.getLoginErrorJson(errorCode));
								}
							});
						}
    
						@Override
						public void onSuccess(OGSdkUser user) {
							// TODO Auto-generated method stub
							final String userJson = user.getMsg();
							Log.d(ThranLoginTag,"thran sdk LoginThird success loginInfo = "+userJson);
							Cocos2dxGLSurfaceView.getInstance().queueEvent(new Runnable() {
								@Override
								public void run() {
									randomNickName("25010");
									onLoginCallback(0, userJson);
								}
							});
						}
					});
				} catch(Exception e) {
					Log.e(ThranLoginTag, "errorcode = "+e);
				}
			}
		});
	}
	
	public static void logout() {
		Log.d(ThranLoginTag, "thran sdk logout!");
		OGMainActivity.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				OGSdkPlatform.onExit(OGMainActivity.getInstance(), OGUtilities.getChannelID());
				//OGSdkPlatform.showH5LogInView(OGMainActivity.getInstance());
			}
		});
	}	
	
	public static void modifyName(final String displayName, final int gender) {
		OGMainActivity.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				OGSdkUser user = OGSdkUser.getInstance();
				Log.d("JNI",  "ModifyName displayName is = " + displayName);
				user.setSex(gender);
				OGSdkPlatform.setUserInfo(OGMainActivity.getInstance(), user.getCerts(), 1, user.getRolename(), displayName, 20, gender, new OGIDispatcherCallback(){

					@Override
					public void onFinished(final String arg0) {
						// TODO Auto-generated method stub
						Log.d("JNI",  "ModifyName setUserInfo OGIDispatcherCallback is = " + arg0);
						Cocos2dxGLSurfaceView.getInstance().queueEvent(new Runnable() {
							@Override
							public void run() {
								onModifyNameCallback(0, arg0);
							}
						});
					}

					@Override
					public void onProcessing(String arg0) {
						// TODO Auto-generated method stub
						
					}
					
				});
			}
		});
	}
	
	public static void getURLFromSDK() {
		OGMainActivity.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					OGSdkPlatform.getServerInfo(OGMainActivity.getInstance(), OGUtilities.getThranSdkAppID(),new OGSdkServerInfo() {
						@Override
						public void onIdentifyResult(String resultInfo) {
//							Log.v(LOGTAG,"thran sdk utiliti getURLFromSDK data = "+resultInfo);
							// TODO Auto-generated method stub
							final String tmpRes = resultInfo;
							Cocos2dxGLSurfaceView.getInstance().queueEvent(new Runnable() {
								@Override
								public void run() {
									onGetURLFromSDK(tmpRes);
								}
							});
						}
					});
				
				}catch(Exception e) {
//					Log.v(LOGTAG,"getURLFromSDK--Exception = "+e);
				}
			}
		});
	}
		
	public static void getGameParamByKey(final String key) {
		Log.v(GetGameParamTag, GetGameParamTag + " key = " + key);
		OGMainActivity.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					OGSdkPlatform.getGameParamByKey(OGMainActivity.getInstance(), key, new OGLoadParamsCallBack() {						
						@Override
						public void onLoad(final String arg0) {
							// TODO Auto-generated method stub
							Log.v(GetGameParamTag, GetGameParamTag + "content json string = " + arg0);
							if (arg0.compareTo("error") != 0) {
								Cocos2dxGLSurfaceView.getInstance().queueEvent(new Runnable() {
									@Override
									public void run() {
										ThranSDKUtils.nativeCommonGameParamByKey(key, arg0);;
									}
								});
							}
						}
					});
				} catch (Exception e) {
					Log.e(GetGameParamTag, "error = " + e);
				}
			}
		});
	}

		public static void downLoadResourceByThranSDK(final String url, final String fileID, final String md5) {
		Log.v(DownLoadTag, "down load url = "+url);
		OGMainActivity.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static void getGameParamByKeys(final String key) {
		OGSdkPlatform.initGameParamsByKey(OGMainActivity.getInstance(),OGUtilities.getThranSdkAppID(),"upgrade|pegift|gameinit|charge|push|popup");
		if(key.isEmpty()) {
			getGameParamByKey("upgrade");
			getGameParamByKey("pegift");
			getGameParamByKey("gameinit");
			getGameParamByKey("charge");
			getGameParamByKey("push");
			getGameParamByKey("popup");
		}else {
			getGameParamByKey(key);
		}
	}
		
    public static void randomNickName(final String gameId)
    {
    	OGMainActivity.getInstance().runOnUiThread(new Runnable() {
    		public void run() {	
    			OGSdkPlatform.getUserNickNames(OGMainActivity.getInstance(), gameId, new OGIDispatcherCallback(){

					@Override
					public void onFinished(final String arg0) {
						// TODO Auto-generated method stub
						Log.d("JNI",  "ModifyName randomNickName onFinished result = " + arg0);
						Cocos2dxGLSurfaceView.getInstance().queueEvent(new Runnable() {
							@Override
							public void run() {
								nativeGetModifyName(arg0);
							}
						});
					}

					@Override
					public void onProcessing(String arg0) {
						// TODO Auto-generated method stub
						Log.d("JNI",  "randomNickName onProcessing info = " + arg0);
					}
    			});
    		}
    	});
    }	
		
		/**
		 * 添加一个本地推送
		 * 
		 * @param context
		 *            上下文对象
		 * @param time
		 *            具体时间,如 09:00
		 * @param interval
		 *            时间间隔,单位s
		 * @param iconRes
		 *            通知的图标
		 * @param ticker
		 *            通知的提示
		 * @param title
		 *            通知的标题
		 * @param msg
		 *            通知的内容
		 * @param actPath
		 *            通知要跳转的目标Activity
		 * @param actExtra
		 *            跳转到目标Activity附带的参数,取该参数也是该名称
		 *            如:intent.getStringExtra("actExtra")
		 * @return 注册的通知的id,取消通知的依据
		 * 
		 */
		public static int registerPushTask(final int hour, final int minute, final long interval, final String ticker, final String title, final String msg, final String actExtra) {
			final String activePath = OGUtilities.getPackageName() + ".LandlordDJ";

			Log.v(LocalPushTag, "start registerPushTask,hour = "+hour+" interval = "+interval+" actExtra = "+actExtra + " activePath = "+activePath+" title = "+title);

			PushEventId = 0;
			OGMainActivity.getInstance().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						PushEventId = OGSdkPlatform.registerPushTask(OGMainActivity.getInstance(), hour, minute, interval, R.drawable.push, ticker, title, msg, activePath, actExtra);
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			return PushEventId;
		}
		
		public static int registerDelayPushTask(final long startAfter, final long interval, final String ticker, final String title, final String msg, final String actExtra) {
			final String activePath = OGUtilities.getPackageName() + ".LandlordDJ";
			Log.v(LocalPushTag, "start registerDelayPushTask,startAfter = "+startAfter+" interval = "+interval+" actExtra = "+actExtra + " activePath = "+activePath+" title = "+title);
			PushEventId = 0;
			OGMainActivity.getInstance().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						PushEventId = OGSdkPlatform.registerDelayPushTask(OGMainActivity.getInstance(), startAfter, interval, R.drawable.push, ticker, title, msg, activePath, actExtra);
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			return PushEventId;
		}
}
