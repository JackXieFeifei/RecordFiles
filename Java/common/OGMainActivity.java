package com.og.common;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.lib.Cocos2dxEditText;
import org.cocos2dx.lib.Cocos2dxHelper;
import org.cocos2dx.lib.Cocos2dxRenderer;

import android.app.ActivityManager;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;
import com.og.common.OGWebView;
import com.og.danjiddz.R;
import com.og.unite.main.OGSdkPlatform;
import com.og.unite.shop.EShopType;
import com.og.unite.shop.OGSdkIShopCenter;
import com.og.unite.shop.bean.OGSDKShopData;
import com.og.unite.PayDetailList.OGSdkPayDetailList;
import com.og.unite.charge.OGSdkIPayCenter;

// Use custom layout to detect soft input keyboard state
final class OGMainLayout extends RelativeLayout {

	public static native void onKeyboardPositionChanged();

	public OGMainLayout(Context context) {
		super(context);
	}

	@Override
	public void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		onKeyboardPositionChanged();
	}
}

public class OGMainActivity extends Cocos2dxActivity {

	public static native void nativeShopListResult(String sMsg, int result);
	public static native void nativeThranSdkPayResult(int result, String sMsg);
	public static native void nativeThranSdkPayChargeRecord(String sMsg);
	public static native void nativeStartByPushEventResult(String startParam);
	
	public static final int Progress_Dialog = 0; // 对话框
	public static final int Progress_Toast = 1; // 对话框toast
	public static final int System_Dialog = 2; // 对话框toast

	private static long dialogtimeout = 60000;//
	private Timer timer;
	private OGWebView mWebView;
	public static OGDialog progressDialog = null;
	public static Builder systemDialog = null;
	private static OGMainActivity mInstance;
    public static boolean isCanGobackBoolean = false;
	private ViewGroup mLayout;
	public RelativeLayout mMainLayout; // 更新弹出框
	private WakeLock mWakeLock;
	public boolean mbWifiChange; // 是否监听wifi的改变
	private OGDownloadAPK mDownload;


	private static String TAG = "OGMainActivity";
	private static String UpgradeTag = "sdk upgrade log";
	private static String PushEventTag = "PushEventTag";
	
	private final static int HANDLER_DOWNLOAD = 1; // 下载
	private final static int HANDLER_TIP = 2; // 浮动提示

	private final static int HANDLER_SHOWDIALOG = 4; // 显示 转圈 dialog
	private final static int HANDLER_HIDEDIALOG = 5; // 隐藏 转圈 dialog
	private final static int HANDLER_SHOWTOAST = 6; // 显示toast
	private final static int HANDLER_SHOWEDITBOX = 7; // 显示输入框
	private final static int HANDLER_HIDEEDITBOX = 8; // 隐藏输入框
	public final static int DELAY_CALL_THRAN_PAY_RES = 9; // 延时调用thransdk支付回调
	public final static int  HANDLER_SHOW_SYSTEM_DIALOG = 10; // 调出系统提示框，点击确定关闭。
	public final static int HANDLER_DOWNLOAD_CLOSE = 11;// 关闭下载提示框
	
	protected static boolean isPlayingBgMusic = false;
	protected static float effectVolume = 0.0f;
	private static boolean isForeground = false;
	
	public int upgrade_mode = -1; //1-提示升级 2-强制升级 
	public int upgrade_switch = 0;//升级开关 0-否，1-是，2-客户端默认
	
	public SharedPreferences m_thranSdkPayResultInfo = null;
	public String channelID = "";
	public String appID = "";
	public String localStartParam = ""; // 带参启动，透传参数
	public static String getuiStartParam = null; // 带参启动，透传参数

	public static OGMainActivity getInstance() {
		return mInstance;
	}
		
	public void init() {
    	// FrameLayout
        ViewGroup.LayoutParams framelayout_params =
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                       ViewGroup.LayoutParams.MATCH_PARENT);
        mLayout = new OGMainLayout(this);
        mLayout.setLayoutParams(framelayout_params);

        // Cocos2dxEditText layout
        ViewGroup.LayoutParams edittext_layout_params =
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                       ViewGroup.LayoutParams.WRAP_CONTENT);
        Cocos2dxEditText edittext = new Cocos2dxEditText(this);
        edittext.setLayoutParams(edittext_layout_params);
        edittext.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        // ...add to FrameLayout
        mLayout.addView(edittext);

        // Cocos2dxGLSurfaceView
        this.mGLSurfaceView = this.onCreateView();

        // ...add to FrameLayout
        mLayout.addView(this.mGLSurfaceView);

        this.mGLSurfaceView.setCocos2dxRenderer(new Cocos2dxRenderer());
        this.mGLSurfaceView.setCocos2dxEditText(edittext);

        // Set frame layout as the content view
		setContentView(mLayout);
	}

	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		m_thranSdkPayResultInfo = getSharedPreferences("thranSdkPayRes", MODE_PRIVATE);
		mInstance = this;
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
				| PowerManager.ON_AFTER_RELEASE, "Ourgame");
		try {
			mWakeLock.acquire();
		} catch (final SecurityException e) {

		}
		 initThranSdk(); 						// init ThranSdk
		 AnalyticsConfig.setChannel(channelID); // umeng安卓统计
		 initAudioVolume();						// 初始化声音音量
		 onPushEventIntent();					// 接收通过intent透传的推送相关数据
	}

	protected void onDestroy() {	
		clearWebviewCache();
		OGSdkPlatform.destroy();
		mWakeLock.release();
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		isForeground = false;
		mWakeLock.release();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		isForeground = true;
		mWakeLock.acquire();
		MobclickAgent.onResume(this);
	}

	@Override
	public void onWindowFocusChanged(final boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
	}

	private boolean detectOpenGLES20() {
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		ConfigurationInfo info = am.getDeviceConfigurationInfo();
		return (info.reqGlEsVersion >= 0x20000);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {		
		if (keyCode == KeyEvent.KEYCODE_BACK
				|| keyCode == KeyEvent.KEYCODE_MENU) {
			if (mInstance.mWebView != null) {
				if (mInstance.mGLSurfaceView != null) {
					return mInstance.mGLSurfaceView.onKeyDown(keyCode, event);
				}
				return true;
			} else {
				return super.onKeyDown(keyCode, event);
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_TIP:
				long toasttimeout = msg.getData().getLong("timeout");
				String tip = msg.getData().getString("tip");
				if (toasttimeout == 0) {
					Toast toast = Toast.makeText(mInstance, tip,
							Toast.LENGTH_LONG);
					toast.show();
				} else {
					Toast toast = Toast.makeText(mInstance, tip,
							Toast.LENGTH_SHORT);
					toast.show();
				}
				break;
			case HANDLER_DOWNLOAD:
				// 下载
				String url = msg.getData().getString("url");
				// OGDownloadAPK down = new OGDownloadAPK(url);
				// down.run();
				if (mDownload == null) {
					mDownload = new OGDownloadAPK(url);
					mDownload.run();

					Toast toast1 = Toast.makeText(mInstance, msg.getData()
							.getString("tip"), Toast.LENGTH_SHORT);
					toast1.show();
				} else {
					// 显示下载进度
					mDownload.onShowProgress();
				}
				break;
			case HANDLER_DOWNLOAD_CLOSE:
				onCloseDownload();
				break;
			case HANDLER_SHOWDIALOG:

				long timeout = msg.getData().getLong("timeout");
				if (timeout > 0) {
					startTimer();
				}
				String tipStr = msg.getData().getString("text");
				if (progressDialog == null) {
					progressDialog = OGDialog.createDialog(OGMainActivity
							.getInstance());
				}
				if (progressDialog != null) {
					progressDialog.setMessage(tipStr);
					progressDialog.show();

				}
				break;
			case HANDLER_SHOW_SYSTEM_DIALOG:
				
//				String sysStr = msg.getData().getString("text");
//				if (systemDialog == null) {
//					systemDialog = new AlertDialog.Builder(mInstance);
//				}
//				if (systemDialog != null) {
//					systemDialog.setMessage(sysStr).setNegativeButton("确定", new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//						}
//					 }).create();
//					systemDialog.show();
//				}
				break;
				
			case HANDLER_HIDEDIALOG:
				stopTimer();
				if (progressDialog != null) {
					progressDialog.dismiss();
					progressDialog = null;
				}
				break;
			case HANDLER_SHOWTOAST:
				String toastStr = msg.getData().getString("text");
				Toast diatoast = Toast.makeText(mInstance, toastStr,
						Toast.LENGTH_LONG);
				diatoast.show();
				break;
				
			case HANDLER_HIDEEDITBOX:
				String str = msg.getData().getString("text");
				// if(mMainLayout)
				if (mMainLayout != null) {
					mMainLayout.removeViewAt(getViewID(R.id.rl_ogeditbox));

				}

				mInstance.mWebView.runJS("javascript:setTextFromClient('" + str
						+ "')");
				break;

			case HANDLER_SHOWEDITBOX:
				LayoutInflater.from(mInstance).inflate(R.layout.ogeditbox,
						mMainLayout);
				EditText editText = (EditText) findViewById(R.id.edit_text);
				editText.requestFocus();
				editText.setOnEditorActionListener(new OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (v != null) {
							if (v.getText() != null) {
								String str = v.getText().toString();
								hideEditBox(str);
							}
						}
						return false;
					}
				});

				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(editText, InputMethodManager.RESULT_SHOWN);
				imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
						InputMethodManager.HIDE_IMPLICIT_ONLY);
				break;
				
				case DELAY_CALL_THRAN_PAY_RES:{
					final String tmpMsg = (String)msg.getData().getString("msg");
			        int res = (int)msg.getData().getInt("result");
					getInstance().onThranSdkPayResult(res, tmpMsg);
					break;
				}
			}
		}
	};
	
	private void startTimer() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
			timer = null;
		}
		timer = new Timer();
		TimerTask tt = new TimerTask() {
			@Override
			public void run() {
				if (timer != null) {
					timer.cancel();
					timer.purge();
				}
				hideSystemProgressDialog();

			}
		};
		if (timer != null) {
			timer.schedule(tt, dialogtimeout, 1);
		}
		//Log.d("Timer", " startTimer");

	}

	private void stopTimer() {
		//Log.d("Timer", " stopTimer");
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
	}
	
	private void initThranSdk() {
		OGSdkPlatform.setConnectLog(true);//是否输出log	
		 OGSdkPlatform.initSDK(this);
		 channelID = OGSdkPlatform.getChannel(this);
		 appID = OGSdkPlatform.getAppId(this);
	}
	
	private void initAudioVolume(){
		try{
			AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			 int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			 int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			 float adjustVolume = 0.0f;
			 if (maxVolume != 0) {
				adjustVolume = (float)volume / (float)maxVolume;
			 }
			 Cocos2dxHelper.setEffectsVolume(adjustVolume);
			 Cocos2dxHelper.setBackgroundMusicVolume(adjustVolume);
		}catch(Exception e) {
    		Log.e("Exception","initAudioVolume error = "+e);
        }
	}
	
	private void onPushEventIntent() {
		// 推送启动游戏, 透传参数
	     Intent intent = this.getIntent();
	     localStartParam = intent.getStringExtra("actExtra");
   	     Log.v(PushEventTag, "PushEventTag local startParam = "+ localStartParam);
	     if(localStartParam != null) {
	    	 Log.v(PushEventTag, "PushEventTag local");
	    	 this.runOnGLThread(new Runnable() {
		            @Override
		            public void run() {
		            	try {
		    				OGMainActivity.nativeStartByPushEventResult(localStartParam);
		            	} catch(Exception e) {
		            		Log.e("Exception","nativeStartByPushEventResult error = "+e);
		            	}
		            }
				});	
	     }else{
	    	 Log.v(PushEventTag, "PushEventTag getui");
	    	 if(getuiStartParam != null) {
		    	 Log.v(PushEventTag, "PushEventTag getui call native method begin");
	    		 this.runOnGLThread(new Runnable() {
			            @Override
			            public void run() {
			            	try {
			    				OGMainActivity.nativeStartByPushEventResult(getuiStartParam);
			            	} catch(Exception e) {
			            		Log.e("Exception","nativeStartByPushEventResult error = "+e);
			            	}
			            }
					});	
	    	 }
	     }
	     OGSdkPlatform.unRegisterAll(this); // 启动游戏停掉所有的本地推送事件倒计时
	}

	public static int getViewID(int iRid) {
		int iRet = -1;
		// 移除最上边的一个视图
		for (int i = 0; i < OGMainActivity.getInstance().mMainLayout
				.getChildCount(); i++) {
			if (iRid == OGMainActivity.getInstance().mMainLayout.getChildAt(i)
					.getId()) {
				iRet = i;
				break;
			}
		}

		return iRet;
	}
	
//	--------------------------local push begin-----------------------------
	public static String getStartParam() {
		return OGMainActivity.getInstance().localStartParam;
	}
	public static String getGetuiStartParam() {
		return OGMainActivity.getuiStartParam;
	}
	public static void setGetuiStartParam(String s) {
		if(s == "") {
			OGMainActivity.getuiStartParam = null;
		}else {
			OGMainActivity.getuiStartParam = s;
		}
	}
//	--------------------------local push end-----------------------------
	
//	--------------------------thran sdk pay begin-----------------------------
	public static void getSDKShopList(final String rolename, final String extendData, final String shopType) {
		OGMainActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	try {
            		EShopType mShopType = EShopType.SHOP;
            		if(shopType.equals("limit")) {
            			mShopType = EShopType.LIMIT;
            		}
            		if(shopType.equals("timelimit")) {
            			mShopType = EShopType.TIMELIMIT;
            		}
            		OGSdkPlatform.getShopListByType(OGMainActivity.getInstance(),rolename,extendData, mShopType, new OGSdkIShopCenter(){
              			 @Override
              			 public void onGetShopListResult(final OGSDKShopData arg0) {
              				OGMainActivity.getInstance().runOnGLThread(new Runnable() {
              		            @Override
              		            public void run() {
              		            	OGMainActivity.nativeShopListResult(arg0.resultJson, arg0.status);
              		            }
              				});	
              			 }
              		 });
            	}catch(Exception e) {
            		Log.e("onGetShopListResult Exception","error = "+e);
                }
            }
		});
	}
	
	public static void updateSDKShopList(final String rolename, final String extendData) {

	}
	
	public static void getThranSDKChargeRecordList(final int chargeType,final String[] rolename) {
		OGMainActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	try{
            		OGSdkPlatform.getPayDetailList(chargeType,OGUtilities.getThranSdkAppID(),new OGSdkPayDetailList(){
        				@Override
        				public void onIdentifyResult(String arg0) {
        					final String tmpMsg = arg0;
        					OGMainActivity.getInstance().runOnGLThread(new Runnable() {
               		            @Override
               		            public void run() {
               		            	OGMainActivity.nativeThranSdkPayChargeRecord(tmpMsg);
               		            }
               				});	
        				}
               		 },rolename); 
            	}catch(Exception e) {
            		Log.e("getThranSDKChargeRecordList Exception","error = "+e);
            	}
            }
		});
	}
	
	public static void payWithThranSdk(final String sdkPackageKey, final String rolename, final String productID, final String orderID, final String extendData, final String payExtendData) {
		Log.v("payWithThranSdk Exception","payExtendData ="+payExtendData);
		mInstance.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					Log.v("payWithThranSdk java","payWithThranSdk java interface: sdkPackageKey ="+ sdkPackageKey+"rolename = "+ rolename+"productID"+productID+"orderID"+orderID+"extendData"+extendData+"payExtendData"+payExtendData);
					OGSdkPlatform.payUI(getInstance(), sdkPackageKey, rolename, productID, orderID ,extendData, payExtendData, new OGSdkIPayCenter(){
						// result支付返回的code，msg，返回具体消息内容。第一期先来确定具体需要的值，暂定为SDK订单号，和透传字段。
						@Override
					    public void onPayResult(int result, String msg) {
							 if (result == 0) {
								 String toastStr = OGMainActivity.getInstance().getString(R.string.pay_result_success);
								 OGMainActivity.showSystemProgressDialog(toastStr,System_Dialog,100000);
							 }
						     Message tmpMsg = new Message();
						     tmpMsg.what = DELAY_CALL_THRAN_PAY_RES;
						     tmpMsg.getData().putString("msg", msg);
						     tmpMsg.getData().putInt("result",result);
						     getInstance().mHandler.sendMessageDelayed(tmpMsg, 1000);
					    }
					});
				} catch(Exception e) {
            		Log.e("payWithThranSdk Exception","error = "+e);
				}
			}
		});
		
	}
	
	public void onThranSdkPayResult(final int result, final String msg) {
		SharedPreferences userInfo = getInstance().m_thranSdkPayResultInfo;  
        userInfo.edit().putString("smsg", msg).commit();
        userInfo.edit().putInt("result", result).commit();
		OGMainActivity.getInstance().runOnGLThread(new Runnable() {
            @Override
            public void run() {
            	try {
            		SharedPreferences userInfo = getInstance().m_thranSdkPayResultInfo;  
                    String smsg = userInfo.getString("smsg", "");
                    int res = userInfo.getInt("result", -1);
    				OGMainActivity.nativeThranSdkPayResult(res, smsg);
            	} catch(Exception e) {
            		Log.e("onThranSdkPayResult Exception","error = "+e);
            	}
            }
		});	
	}
	
	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data){  
		 super.onActivityResult(requestCode,resultCode,data);   
		 ThranSDKUtils.onAccountDialogResult(requestCode, resultCode, data);
     } 
	
//	--------------------------thran sdk pay end-----------------------------
	
//	--------------------------thran sdk upgrade end-----------------------------


	public static int  getUpgradeSwitch() {
		return OGMainActivity.getInstance().upgrade_switch;
	}
	
	public static int  getUpgradeMode() {
		return OGMainActivity.getInstance().upgrade_mode;
	}
	
//	--------------------------thran sdk upgrade end-----------------------------
	
	
//	--------------------------umeng sdk begin-----------------------------
	public static void addUmengEvent(String eventID) {
		try {
			MobclickAgent.onEvent(mInstance, eventID);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void addUmengEvent(String eventID,String value) {
		try {
			MobclickAgent.onEvent(mInstance, eventID,value);
		} catch(Exception e) {
			e.printStackTrace();
		}		
	}
	
	public static void addNewUmengEvent(String eventID,String source,String value) {
		try {
			Log.e("addNewUmengEvent","eventID = "+eventID);
			Log.e("addNewUmengEvent","source = "+source);
			Log.e("addNewUmengEvent","value = "+value);
			HashMap<String,String> map = new HashMap<String,String>();
			map.put(source+"",value+"");
			MobclickAgent.onEvent(mInstance, eventID , map);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void addUmengEvent(String eventID,String source,String stall) {
		try {
			HashMap<String,String> map = new HashMap<String,String>();
			map.put("behavior",source);
			map.put(source,stall); 
			MobclickAgent.onEvent(mInstance, eventID , map);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void addUmengEvent(String eventID,int level,int sublevel,int value) {
		try {
			HashMap<String,String> map = new HashMap<String,String>();
			map.put("level",level+"");
			map.put("sublevel",sublevel+""); 
			map.put("value", value+"");
			MobclickAgent.onEvent(mInstance, eventID , map);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
//	--------------------------umeng sdk end-----------------------------
	
	
//	--------------------------webview  begin-----------------------------

	public static void showWebView(final int checknet, final int x,
			final int y, final int width, final int height, final byte[] url) {
		mInstance.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String urlStr = null;
				try {
					urlStr = new String(url, "UTF-8");
				} catch (Exception e) {
					urlStr = new String(url);
				}
				if (mInstance.mWebView != null) {
					mInstance.mWebView.closeView();
					mInstance.mWebView = null;
				}
				mInstance.mWebView = new OGWebView(mInstance);
				mInstance.mWebView.initView(mInstance.mLayout, checknet, x, y,
						width, height, urlStr);
			}			
		});
	}
	
	public static void showWebViewWithContent(final int checknet, final int x,
			final int y, final int width, final int height, final byte[] content) {
		mInstance.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String contentStr = null;
				try {
					contentStr = new String(content, "UTF-8");
				} catch (Exception e) {
					contentStr = new String(content);
				}
				if (mInstance.mWebView != null) {
					mInstance.mWebView.closeView();
					mInstance.mWebView = null;
				}
				mInstance.mWebView = new OGWebView(mInstance);
				mInstance.mWebView.initViewWithContent(mInstance.mLayout, checknet, x, y,
						width, height, contentStr);
			}
		});
	}

	private void clearWebviewCache() {
		// File file = CacheManager.getCacheFileBaseDir();
		// if (file != null && file.exists() && file.isDirectory()) {
		// for (File item : file.listFiles()) {
		// item.delete();
		// }
		// file.delete();
		// }

		deleteDatabase("webview.db");
		deleteDatabase("webviewCache.db");
	}

	public static void hideWebView() {
		mInstance.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mInstance.mWebView != null) {
					mInstance.mWebView.closeView();
				}
				mInstance.mWebView = null;
			}
		});
	}

	public static void refreshWebView() {
		mInstance.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mInstance.mWebView != null) {
					mInstance.mWebView.reloadView();
				}
			}
		});
	}
	public static boolean canGobackWebView() {
		mInstance.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mInstance.mWebView != null) {
				isCanGobackBoolean=	mInstance.mWebView.canGobackTowebView();
				}
				else 
				{
					isCanGobackBoolean=false;
				}
			}
		});
		return isCanGobackBoolean;
    }
	
	public static void setWebViewVisible(final boolean flag) {
		mInstance.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mInstance.mWebView != null) {
					if(flag) {
						mInstance.mWebView.setVisible(View.VISIBLE);
					}else {
						mInstance.mWebView.setVisible(View.GONE);
					}
				}
			}
		});
    }
	
	public static void gobackWebView() {
		mInstance.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mInstance.mWebView != null) {
				    mInstance.mWebView.gobackTowebView();
				}
			}
		});
    }
	
//	--------------------------webview  end-----------------------------



	// ---------------------------------登录    begin-----------------------------------
	public static void login(final int loginType, final byte[] appID,
			final byte[] apiKey, final byte[] secretKey, final byte[] account,
			final byte[] password) {
//		ThirdAbstract third = ThirdFactory.getThirdInstance(mInstance,
//				loginType);
		String appIDStr = null;
		String apiKeyStr = null;
		String secretStr = null;
		String accountStr = null;
		String passwordStr = null;
		try {
			appIDStr = new String(appID, "UTF-8");
			apiKeyStr = new String(apiKey, "UTF-8");
			secretStr = new String(secretKey, "UTF-8");
			accountStr = new String(account, "UTF-8");
			passwordStr = new String(password, "UTF-8");
		} catch (Exception e) {
		}

//		third.login(appIDStr, apiKeyStr, secretStr, accountStr, passwordStr);
	}

	public static void showLoginView(final int loginType, final byte[] appID,
			final byte[] apiKey, final byte[] secretKey) {
		mInstance.runOnUiThread(new Runnable() {
			@Override
			public void run() {
//				ThirdAbstract third = ThirdFactory.getThirdInstance(mInstance,
//						loginType);
				String appIDStr = null;
				String apiKeyStr = null;
				String secretStr = null;
				try {
					appIDStr = new String(appID, "UTF-8");
					apiKeyStr = new String(apiKey, "UTF-8");
					secretStr = new String(secretKey, "UTF-8");
				} catch (Exception e) {
				}

//				third.addLoginView(appIDStr, apiKeyStr, secretStr);
			}
		});
	}

	// -------------------------------------登录    end------------------------------------------------

	static {
		System.loadLibrary("game");
	}
	
	public static void showSystemProgressDialog(String val, int type,
			int timeout) {
		if (val == null) {
			val = "请稍候...";
		}
		dialogtimeout = timeout;
		final String tipStr = val;
		Message msg = new Message();
		if (type == Progress_Toast) {
			msg.what = HANDLER_SHOWTOAST;
			msg.getData().putLong("timeout", dialogtimeout);
			msg.getData().putString("text", tipStr);
			if (getInstance().mHandler != null) {
				getInstance().mHandler.sendMessage(msg);
			}
		} else if(type == Progress_Dialog){
			msg.what = HANDLER_SHOWDIALOG;
			msg.getData().putString("text", tipStr);
			msg.getData().putLong("timeout", dialogtimeout);
			if (getInstance().mHandler != null) {
				getInstance().mHandler.sendMessage(msg);
			}
		} else {
			msg.what = HANDLER_SHOW_SYSTEM_DIALOG;
			msg.getData().putString("text", tipStr);
			msg.getData().putLong("timeout", dialogtimeout);
			if (getInstance().mHandler != null) {
				getInstance().mHandler.sendMessage(msg);
			}
		}
	}

	public static void hideSystemProgressDialog() {

		Message msg = new Message();
		msg.what = HANDLER_HIDEDIALOG;

		if (getInstance().mHandler != null) {
			getInstance().mHandler.sendMessage(msg);
		}

	}

	public static void showEditBox() {
		//Log.d("activity", "showEditBox");

		Message msg = new Message();
		msg.what = HANDLER_SHOWEDITBOX;
		if (getInstance().mHandler != null) {
			getInstance().mHandler.sendMessage(msg);
		}

	}

	public static void hideEditBox(String str) {

		//Log.d("activity", "showEditBox");

		Message msg = new Message();
		msg.what = HANDLER_HIDEEDITBOX;
		if (getInstance().mHandler != null) {
			msg.getData().putString("text", str);

			getInstance().mHandler.sendMessage(msg);
		}

	}
	//--------------------------------down load apk begin--------------------------------
	public static void onDownloadApp(String url) {
		//Log.i(TAG, "[onDownloadApp].url = " + url);

		Message msg = new Message();
		if (OGUtilities.getSD()) {
			msg.what = HANDLER_DOWNLOAD;
			msg.getData().putString("url", url);
			msg.getData().putString("tip",
					mInstance.getString(R.string.tip_get_update));

			getInstance().mHandler.sendMessage(msg);
		} else {
			// no sd
			msg.what = HANDLER_TIP;
			msg.getData().putString("tip",
					mInstance.getString(R.string.error_tip_sd_exit));

			getInstance().mHandler.sendMessage(msg);
		}
	}

	/**
	 * 取消安装
	 */
	public static void onCloseDownload() {
		if (getInstance().mDownload != null) {
			getInstance().mDownload.release();
			getInstance().mDownload = null;
		}
	}
	//--------------------------------down load apk end----------------------------------
}    
