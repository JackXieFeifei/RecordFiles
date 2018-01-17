package com.og.common;

import com.igexin.sdk.PushConsts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * 个推透传参数
 * 
 * 
 * @author xuefr
 */
public class OGGetuiReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {		

		Bundle bundle = intent.getExtras();
		switch (bundle.getInt(PushConsts.CMD_ACTION)) {
				case PushConsts.GET_MSG_DATA:
					byte[] payload = bundle.getByteArray("payload");
					if (payload != null) {
				    	OGMainActivity.setGetuiStartParam(new String(payload));
					}
					if(OGMainActivity.getuiStartParam != null && OGMainActivity.getuiStartParam != "") {
				    	Log.v("nativeStartByPushEventResult", "PushTask getui startParam = "+ OGMainActivity.getuiStartParam);
				     }else{
				    	 Log.v("nativeStartByPushEventResult", "PushTask getui has no start param");
				     }
					break;
				default:
					break;
			}
	}
}
