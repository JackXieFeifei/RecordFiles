/****************************************************************************
Copyright (c) 2010-2012 cocos2d-x.org

http://www.cocos2d-x.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
****************************************************************************/
package com.og.danjiddz;
import org.cocos2dx.lib.Cocos2dxGLSurfaceView;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

import com.duoku.platform.single.DKPlatform;
import com.duoku.platform.single.DkErrorCode;
import com.duoku.platform.single.DkProtocolKeys;
import com.duoku.platform.single.DKPlatformSettings;
import com.duoku.platform.single.callback.IDKSDKCallBack;

import com.og.common.OGMainActivity;
import com.og.common.ThranSDKUtils;
import com.og.unite.data.OGSdkUser;
import com.og.unite.login.OGSdkIUCenter;
import com.og.unite.main.OGSdkPlatform;



public class LandlordDJ extends OGMainActivity {
	
	private static LandlordDJ mInstance;
	
	public static LandlordDJ getInstance() {
		return mInstance;
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mInstance = this;
		
		IDKSDKCallBack initcompletelistener = new IDKSDKCallBack() {
			@Override
			public void onResponse(String paramString) {
				// TODO Auto-generated method stub
				Log.d("88888", "success01");
				try {
					Log.d("88888", "success02");
					JSONObject jsonObject = new JSONObject(paramString);
					// 返回的操作状态码
					int mFunctionCode = jsonObject.getInt(DkProtocolKeys.FUNCTION_CODE);
					
					//初始化完成
					if(mFunctionCode == DkErrorCode.BDG_CROSSRECOMMEND_INIT_FINSIH) {
						Log.d("88888", "success03");
						initAds();
//						Intent intent = new Intent(GameFirstActivity.this, GameMainActivity.class);
//						startActivity(intent);
//						GameFirstActivity.this.finish();

						
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		//参数为测试数据，接入时请填入你真实数据
//		DKCMMMData mmData = new DKCMMMData("30000834443901","AF54ED9926C2ADAF");
//		DKCMGBData gbData = new DKCMGBData();
		
		//初始化函数
		DKPlatform.getInstance().init(this, true, DKPlatformSettings.SdkMode.SDK_BASIC, null, null, initcompletelistener);
	}
	
	private void initAds(){
		DKPlatform.getInstance().bdgameInit(this, new IDKSDKCallBack() {
			@Override
			public void onResponse(String paramString) {
				Log.d("GameMainActivity","bggameInit success");
			}
		});
	}
	
	public void onResume() {
		super.onResume();
		mInstance = this;
		DKPlatform.getInstance().resumeBaiduMobileStatistic(this);
	}
	
	public void onPause() {
		super.onPause();
		mInstance = this;
		DKPlatform.getInstance().pauseBaiduMobileStatistic(this);
	}
	
	public void doSdkLoginThird(String channel) {
		final String str = channel;
		OGMainActivity.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					OGSdkPlatform.login(OGMainActivity.getInstance(), str, new OGSdkIUCenter() {
						@Override
						public void onError(int arg0) {
							// TODO Auto-generated method stub
							Log.d("8888", "======== onError = ");
						}
			            
						@Override
						public void onSuccess(OGSdkUser user) {
							// TODO Auto-generated method stub
							final String userJson = user.getMsg();
							Log.d("8888", "======== onSuccess = userJso" + userJson);
							Cocos2dxGLSurfaceView.getInstance().queueEvent(new Runnable() {
								@Override
								public void run() {
									ThranSDKUtils.onLoginCallback(0, userJson);
								}
							});
						}
					});
				} catch(Exception e) {
            		Log.e("doSdkLoginThird Exception","error = "+e);
				}
			}
		});
	}
}
