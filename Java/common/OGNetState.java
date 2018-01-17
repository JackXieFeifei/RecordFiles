package com.og.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

/**
 * 网络状态监听
 * 
 * 通知触发条件： 
 * 有网络 <——互相切换——> 无网络
 * Wifi <——互相切换——> GPRS
 * 
 * @author youyr@ourgame.com 20130609
 */
public class OGNetState extends BroadcastReceiver {
	
	private static native void nativeNetSucks();
	private boolean mbNetSucks = false;

	@Override
	public void onReceive(Context context, Intent intent) {
//		ConnectivityManager connectivityManager = (ConnectivityManager) context
//				.getSystemService(Context.CONNECTIVITY_SERVICE);
//		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
//		NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		
//		// 当收到网络变化通知就开始检测网络可用性
//		int netState = LandlordDJ.isNetworkAvailable();	// 当前网络（0:无网络，1:2G，2:3G/wifi）
//		if (netState == 0){
//			// 无网络
//		}else{
//		}
		
		if (OGMainActivity.getInstance() != null) {
			if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
				if (!OGUtilities.isNetworkAvailable()) {
					//Log.i("", "[nativeNetSucks]"+mbNetSucks);
					// 无网络
					if (mbNetSucks){
						mbNetSucks = false;
						OGMainActivity.getInstance().runOnGLThread(new Runnable() {
				            @Override
				            public void run() {
				            	nativeNetSucks();
				            }
						});
					}
				}else{
					mbNetSucks = true;
				}
			}
			
			//Log.i("", "[onReceive].mbWifiChange="+OGMainActivity.getInstance().mbWifiChange+"/"+intent.getAction()+"/"+WifiManager.WIFI_STATE_CHANGED_ACTION);
			
			if (OGMainActivity.getInstance().mbWifiChange && 
					intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
				
				// WIFI开关
				int wifistate = intent.getIntExtra(
						WifiManager.EXTRA_WIFI_STATE,
						WifiManager.WIFI_STATE_DISABLED);
				
				//Log.i("", "[onReceive].mbWifiChange="+OGMainActivity.getInstance().mbWifiChange+"/wifistate = "+wifistate);
				if (wifistate == WifiManager.WIFI_STATE_DISABLED) {
					// 如果关闭
					OGMainActivity.getInstance().mbWifiChange = false;
				}else{
					// 如果打开
					
					// 变成关闭
					OGUtilities.closeWifi();
				}
			}

		}
	}

}
