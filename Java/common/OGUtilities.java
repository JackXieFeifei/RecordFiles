package com.og.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.Exception;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Locale;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.cocos2dx.lib.Cocos2dxHelper;

import cn.egame.terminal.paysdk.EgameExitListener;
import cn.egame.terminal.paysdk.EgamePay;

import com.excelliance.kxqp.sdk.IQueryUpdateCallback;
import com.og.unite.data.OGSdkUser;
import com.og.unite.main.OGSdkPlatform;
import com.excelliance.kxqp.sdk.GameSdk;


public class OGUtilities {
	public static long BLOCK_SIZE = 1;
	public static String LOGFILESPATH = "log files path";
	public static final String DBTAG = "DBTAG";
	private static int UPDATE_TYPE = 0; 
	
	public static final int CARRIER_UNKNOW = -1;// 未知运营商
	public static final int CARRIER_CMCC = 1;// 中国移动
	public static final int CARRIER_CU = 2;// 中国联通
	public static final int CARRIER_CT = 3;// 中国电信
	
	/**
	 * 当前设备的网络环境
	 */
	public final static int NETWORK_NO = 0; // 无网络
	public final static int NETWORK_WIFI = 1; // wifi
	public final static int NETWORK_2G = 2; // 2g
	public final static int NETWORK_3G = 3; // 3g
	public final static int NETWORK_4G = 4; // 4g
	public final static int NETWORK_ETHERNET = 9; // 有限网络
	
	private static native void noticeMacAddress(String mac);
	
	
	public static byte[] convertUTF8ToGBK(final byte[] str) {
		try {
			String utf8 = new String(str, "UTF-8");
			return utf8.getBytes("GBK");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static byte[] convertGBKToUTF8(final byte[] str) {
		try {
			String gbk = new String(str, "GBK");
			return gbk.getBytes("UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void shutDown() {
		OGMainActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    OGMainActivity.getInstance().onDestroy();
					android.os.Process.killProcess(android.os.Process.myPid());
                } catch (Exception e) {
                    
                }
            }
        });
	}
	
	public static String getDeviceModel() {
		return Build.MODEL;
	}
	
	public static String getPackageName() {
		return Cocos2dxHelper.getCocos2dxPackageName();
	}
	
	/**
	 * 判断是否有SD卡
	 */
	public static boolean getSD() {
		if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment
				.getExternalStorageState())) {
			return true;
		} else{
			return false;
		}
	}
	
	/**
	 * 获取指定图层在容器中的下标
	 * 
	 * @param iRid - 图层的资源id
	 * @return - true：移除图层成功，false：失败
	 */
	public static int getViewID(int iRid){
		int iRet = -1;
		
		//移除最上边的一个视图
		for(int i = 0; i < OGMainActivity.getInstance().mMainLayout.getChildCount(); i++){
			if (iRid == OGMainActivity.getInstance().mMainLayout.getChildAt(i).getId()){
				iRet = i;
				break;
			}
		}
		
		return iRet;
	}
	
	/**
	 * 移除指定的视图界面
	 * 
	 * @param id
	 * @return
	 */
	public static boolean removeView(int iViewID){
		boolean bRet = false;
		
		int id = getViewID(iViewID);
		if (id != -1){
			OGMainActivity.getInstance().mMainLayout.removeViewAt(id);
			bRet = true;
		}
		
		return bRet;
	}
	
	/**
	 * 获取mac地址
	 * @return
	 */
	public static String getMac(){
//		getSDCapacity();
		
		String sRet = null;
		try{
//			public int checkCallingPermission("")
			final WifiManager wifi = (WifiManager) OGMainActivity.getInstance().getSystemService(Context.WIFI_SERVICE);
		    
			//Log.i("", "[getMac].wifi="+wifi);
			if (wifi != null){
				if (!wifi.isWifiEnabled()) {
					OGMainActivity.getInstance().mbWifiChange = true;
					
					wifi.setWifiEnabled(true);
				}else{
					WifiInfo info = wifi.getConnectionInfo();
					if (info != null){
						sRet = info.getMacAddress();
					}
				}
			}
		}catch(Exception e){
		}
		
		return sRet;
	}
	
	/**
	 * 关闭wifi
	 */
	public static void closeWifi(){
		WifiManager wifi = (WifiManager) OGMainActivity.getInstance().getSystemService(Context.WIFI_SERVICE);
		//Log.i("", "[closeWifi].wifi="+wifi);
		if (wifi != null){
			//Log.i("", "[closeWifi].wifi.e="+wifi.isWifiEnabled());
			
			final WifiInfo info1 = wifi.getConnectionInfo();
			//Log.i("", "[closeWifi].info1="+info1);
			if (info1 != null){
				//Log.i("", "[closeWifi].info1.getMacAddress()="+info1.getMacAddress());
				
				OGMainActivity.getInstance().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// 通知已经取到mac
						noticeMacAddress(info1.getMacAddress());
					}
				});
				
				wifi.setWifiEnabled(false);
			}
		}
	}
	
	/**
	 * 返回urlEncode的数据
	 * @return
	 */
	public static String getUrlEncode(String url){
		String sRet = null;
		
		try {
			sRet = URLEncoder.encode(url, "utf-8");
		} catch (UnsupportedEncodingException e) {
		}
		
		return sRet;
	}
	
	/**
	 * 获取sd的容量
	 * 
	 * 受API版本限制可能不准确，但是算剩余容量够了
	 * @return
	 */
	public static long getSDCapacity(){
		long iRet = -1L;
		if (getSD()){
			String sdcard = android.os.Environment.getExternalStorageDirectory().getPath(); 
			File file = new File(sdcard); 
			StatFs statFs = new StatFs(file.getPath()); 
//			iRet = (int) (statFs.getBlockSize()*((long)statFs.getAvailableBlocks()-4));
//			long ll = (statFs.getBlockSize()*((long)statFs.getAvailableBlocks()-4));
			
			iRet = (long)(statFs.getAvailableBlocks()) * (long)(statFs.getBlockSize());
			BLOCK_SIZE = statFs.getBlockSize();
//			Log.i("haha", iRet+"//"+"/"+statFs.getAvailableBlocks()+"/"+statFs.getBlockSize());
		}
		
		return iRet;
	}
	
	public static long getMobileCapacity(){
		File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;		
	}
	
	/**
	 * SD卡写日志
	 * @param str
	 */
	public static void writeLog(byte[] str){
		//Log.i("[OGUtilities]", "[writeLog]");
		if (getSD()){
			String sSD = Environment.getExternalStorageDirectory().toString();
			File filetemp = new File(sSD + "/download/");
			if (!filetemp.exists()) {
				filetemp.mkdir();
			}
			filetemp = new File(sSD + "/download/ourgame/");
			if (!filetemp.exists()) {
				filetemp.mkdir();
			}
			filetemp = new File(sSD + "/download/ourgame/log");
			if (!filetemp.exists()) {
				filetemp.mkdir();
			}
			
			Calendar calendar = Calendar.getInstance(Locale.CHINESE);
			String name = calendar.get(Calendar.YEAR)+"-"
					+(calendar.get(Calendar.MONTH) + 1)+"-"
			        +calendar.get(Calendar.DAY_OF_MONTH);
			
			File mSaveFile = new File(sSD + "/download/ourgame/log", name + ".txt");

			if (!mSaveFile.exists()) {
			//	Log.i("[OGUtilities]", "[writeLog].create");
				filetemp.mkdir();
			}
			//Log.i("[OGUtilities]", "[writeLog].1");
			// 写日志
			if (str != null && str.length > 0){
				try {
					//Log.i("[OGUtilities]", "[writeLog].2");
					RandomAccessFile threadfile;
					threadfile = new RandomAccessFile(mSaveFile, "rw");
					threadfile.length();
					threadfile.seek(threadfile.length());//0);
					threadfile.write(str, 0, str.length);
					threadfile.close();
				} catch (Exception e) {
					//Log.i("[OGUtilities]", "[writeLog].x"+e.toString());
				}
			}
		}
	}
		
	// 获得系统总内存
    public static int getTotalMemory() {
        int total = 0;
        String content = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/meminfo"), 8);
            content = reader.readLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        try {
	        int beginIndex = content.indexOf(':');
	        int endIndex = content.indexOf('k');
	        content = content.substring(beginIndex + 1, endIndex).trim();
	        total = Integer.parseInt(content);
        } catch (Exception e) {
        		ActivityManager am = (ActivityManager)OGMainActivity.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
        		MemoryInfo mi = new MemoryInfo();
        		am.getMemoryInfo(mi);
        		total = (int)(mi.totalMem / 1024);
        }
        return total;
    }
    
	public static String getAppVersion() {
	    try {
	        PackageManager manager = OGMainActivity.getInstance().getPackageManager();
	        PackageInfo info = manager.getPackageInfo(OGMainActivity.getInstance().getPackageName(), 0);
	        String version = info.versionName;
	        return  version;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "1.0.0";
	    }
	}
	
	public static boolean getSimCardState() {
		try {
			TelephonyManager tm = (TelephonyManager)OGMainActivity.getInstance().getSystemService(Context.TELEPHONY_SERVICE);//取得相关系统服务
	        switch(tm.getSimState()){ //getSimState()取得sim的状态  有下面6中状态
	        	case TelephonyManager.SIM_STATE_READY :
	                	return true;
	        	default:
	                	return false;
	        }
		} catch(Exception e) {
			e.printStackTrace();
	        return false;
		}
	}
	
	public static String getMobileNum(){
		String tel = "";
		try {
			TelephonyManager tm = (TelephonyManager)OGMainActivity.getInstance().getSystemService(Context.TELEPHONY_SERVICE);//取得相关系统服务
			if(tm!=null){
				tel=tm.getLine1Number();
			}
		} catch(Exception e) {
			e.printStackTrace();
			tel = "";
		}
		if(tel == null) {
			tel = "";
		}else if(tel.equals("0")) {
			tel = "";
		}
		return tel;
	}
	
	public static String getMobileIMEI(){
		String imei = "";
		try {
			TelephonyManager tm = (TelephonyManager)OGMainActivity.getInstance().getSystemService(Context.TELEPHONY_SERVICE);//取得相关系统服务
			if(tm!=null){
				imei=tm.getDeviceId();
			}
		} catch(Exception e) {
			e.printStackTrace();
			imei = "";
		}

		return imei;
	}
	
	public static String getMobileIMSI(){
		String imsi = "";
		try {
			TelephonyManager tm = (TelephonyManager)OGMainActivity.getInstance().getSystemService(Context.TELEPHONY_SERVICE);//取得相关系统服务
			if(tm!=null){
				imsi= tm.getSubscriberId();
			}
		} catch(Exception e) {
			e.printStackTrace();
			imsi = "";
		}

		return imsi;
	}
	
	/**
	 * 判断是否是移动卡支付
	 */
	public static boolean isYDCard(){
		boolean bRet = false;
		TelephonyManager tm = (TelephonyManager)OGMainActivity.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = tm.getSubscriberId();

		if(imsi!=null){
			if(imsi.startsWith("46000") || imsi.startsWith("46002") || imsi.startsWith("46007")){ 
				// 因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号 //中国移动
				bRet = true;
			}else if(imsi.startsWith("46001")){
				// 中国联通
			}else if(imsi.startsWith("46003")){
				// 中国电信
			}
		}
		
		return bRet;
	}
	
	/**
	 * 获取运营商信息
	 * @return 0：读取不到SIM卡，1：移动，2：联通，3：电信
	 */
	public static int getMobileID() {
		int iRet = CARRIER_UNKNOW;
		try {
			TelephonyManager telephonyManager = (TelephonyManager) OGMainActivity.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
			String IMSI = telephonyManager.getSubscriberId();
			if (IMSI == null) {
				if (TelephonyManager.SIM_STATE_READY == telephonyManager.getSimState()) {
					String operator = telephonyManager.getSimOperator();
					if (operator != null) {
						if (operator.equals("46000") || operator.equals("46002") || operator.equals("46007")) {
							iRet = CARRIER_CMCC;
						} else if (operator.equals("46001") || operator.equals("46006")) {
							iRet = CARRIER_CU;
						} else if (operator.equals("46003") || operator.equals("46005")) {
							iRet = CARRIER_CT;
						}
					}
				}
			} else {
				if (IMSI.startsWith("46000") || IMSI.startsWith("46002") || IMSI.startsWith("46007")) {
					iRet = CARRIER_CMCC;
				} else if (IMSI.startsWith("46001") || IMSI.startsWith("46006")) {
					iRet = CARRIER_CU;
				} else if (IMSI.startsWith("46003") || IMSI.startsWith("46005")) {
					iRet = CARRIER_CT;
				}
			}
		} catch (Exception e) {
		}
		return iRet;
	}
	
	public static void makeCall(String number) {
		try {
			Intent intent=new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+number));
			OGMainActivity.getInstance().startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getSessionID() {
		try {
			OGSdkUser.getInstance().getSessionId();
			String str = OGSdkUser.getInstance().getmSessionID();
			if(str != null) {
				return str;
			} else {
				return "";
			}
		} catch(Exception e) {
    		Log.e("onThranSdkPayResult Exception","error = "+e);
			return "";
		}
	}
	
	public static boolean is3GAvailable() {
		boolean flag = false;
		try {
			ConnectivityManager mgr = (ConnectivityManager) OGMainActivity.getInstance()
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = mgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			//Log.i(TAG, "info = " + info);
			if (info != null) {
				//Log.i(TAG, "info.isAvailable() = " + info.isAvailable());
				flag = info.isAvailable();
			}
		} catch(Exception e) {
			e.printStackTrace();
			flag = false;
		}
		
		return flag;
	}

	public static boolean isWifiAvailable() {
		boolean flag = false;
		try {
			ConnectivityManager mgr = (ConnectivityManager) OGMainActivity.getInstance()
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = mgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (info != null) {
				flag = info.isAvailable();
			}
		} catch(Exception e) {
			e.printStackTrace();
			flag = false;
		}
		
		return flag;
	}

	/**
	 * check network
	 * 
	 * @return false:no network
	 */
	public static boolean isNetworkAvailable() {
		boolean bRet = false;
		try {
			ConnectivityManager conMan = (ConnectivityManager) OGMainActivity.getInstance()
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (conMan != null){
				NetworkInfo info = conMan.getActiveNetworkInfo();
				
				bRet = (info != null && info.isAvailable());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return bRet;
	}

	public static int getNetworkType() {
		int netFlag = NETWORK_NO;
		try {
			ConnectivityManager cm = (ConnectivityManager) OGMainActivity.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
			if (cm != null) {
				NetworkInfo mobNetInfoActivity = cm.getActiveNetworkInfo();
				if (mobNetInfoActivity != null) {
					int type = mobNetInfoActivity.getType();
					if (type == ConnectivityManager.TYPE_WIFI) {
						netFlag = NETWORK_WIFI;
					} else if (type == ConnectivityManager.TYPE_MOBILE) {
						TelephonyManager telephonyManager = (TelephonyManager) OGMainActivity.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
						if (telephonyManager == null)
							return netFlag;
						switch (telephonyManager.getNetworkType()) {
						case TelephonyManager.NETWORK_TYPE_1xRTT:
						case TelephonyManager.NETWORK_TYPE_CDMA:
						case TelephonyManager.NETWORK_TYPE_EDGE:
						case TelephonyManager.NETWORK_TYPE_GPRS:
						case TelephonyManager.NETWORK_TYPE_IDEN:
							netFlag = NETWORK_2G;
							break;
						case TelephonyManager.NETWORK_TYPE_UMTS:
						case TelephonyManager.NETWORK_TYPE_EVDO_0:
						case TelephonyManager.NETWORK_TYPE_EVDO_A:
						case TelephonyManager.NETWORK_TYPE_HSDPA:
						case TelephonyManager.NETWORK_TYPE_HSUPA:
						case TelephonyManager.NETWORK_TYPE_HSPA:
						case TelephonyManager.NETWORK_TYPE_EVDO_B:
						case TelephonyManager.NETWORK_TYPE_EHRPD:
						case TelephonyManager.NETWORK_TYPE_HSPAP:
							netFlag = NETWORK_3G;
							break;
						case TelephonyManager.NETWORK_TYPE_LTE:
							netFlag = NETWORK_4G;
							break;
						default:
							netFlag = NETWORK_2G;
							break;
						}
					} else if (type == ConnectivityManager.TYPE_ETHERNET) {
						netFlag = NETWORK_ETHERNET;
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return netFlag;
	}

	/**
	 * 获取手机sd卡路径 如果sd不存在 则返回data/data文件夹路径
	 * 
	 * @return
	 */
	public static String getSDCardPath() {
		String path = "";
		try {
			boolean sdCardExist = Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED);
			String updatePath = "/updatedata/";
			if (sdCardExist) {
				File sdCardFile = Environment.getExternalStorageDirectory();
				path = sdCardFile.getAbsolutePath() + updatePath;
			} else {
				File dataFile = OGMainActivity.getInstance().getFilesDir();
				path = dataFile.getAbsolutePath() + updatePath;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return path;
	}

	/**
	 * 列举parentPath文件夹下的所有子文件 并把文件名传回
	 * 
	 * @param parentPath
	 * @return
	 */
	public static String listAssertPath(String parentPath) {
		try {
			String[] fileNamesTemp = OGMainActivity.getInstance().getAssets().list(parentPath);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < fileNamesTemp.length; i++) {
				String fileName = parentPath + "/" + fileNamesTemp[i];
				sb.append(fileName).append(",");
			}
			if (sb.length() > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}

			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getChannelID() {
		String channel = getMetaData("OG_APPCHANNEL");
		return channel;
	}
	
	public static String getEgameChannel() {
		String egameChannel = getMetaData("EGAME_CHANNEL");
		return egameChannel;
	}
    
    public static String getMetaData(String key) {
        String MetaData = "";
        try {
            String packageName = OGMainActivity.getInstance().getPackageName();
            PackageManager mgr = OGMainActivity.getInstance().getPackageManager();
            ApplicationInfo info = mgr.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            Object value = info.metaData.get(key);
            if (value != null) {
                MetaData = value.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return MetaData;
    }

	public static String getThranSdkAppID() {
		String appid = getMetaData("OG_APPID");
		return appid;
	}
	
	public static String getThranSdkAppKey() {
		String appkey = getMetaData("OG_APPKEY");
		return appkey;
	}
	
	public static String getThranSdkAppChannel() {
		String appchannal = getMetaData("OG_APPCHANNEL");
		return appchannal;
	}
	
	public static String getGameID() {
		String gid = getMetaData("GAME_ID");
		return gid;
	}
	
	public static String importDataBase() {
		String sRet = "A";
		try {
			if (OGDataBase.isDataBaseExist()) {
				OGDataBase.initDataBase(OGMainActivity.getInstance());
				if (OGDataBase.importData()) {
					sRet = OGDataBase.exportToJson();
				}
				OGDataBase.closeDataBase();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return sRet;
	}
	
	public static void showAppStore(String packageName){
		try {
			Uri uri = Uri.parse("market://details?id="+packageName);
			Intent it = new Intent(Intent.ACTION_VIEW, uri); 
			OGMainActivity.getInstance().startActivity(it);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
    public static void showBrowser(String url){
    	try {
    	    Uri uri = Uri.parse(url);
    	    OGMainActivity.getInstance().startActivity(new Intent(Intent.ACTION_VIEW,uri));
		} catch(Exception e) {
			e.printStackTrace();
		}
    }

    public static String getUmengAppKey() {
		String appid = "danjiddz";
		try {
			String packageName = OGMainActivity.getInstance().getPackageName();
			PackageManager mgr = OGMainActivity.getInstance().getPackageManager();
			ApplicationInfo info = mgr.getApplicationInfo(packageName,
					PackageManager.GET_META_DATA);
			Object value = info.metaData.get("UMENG_APPKEY");
			if (value != null) {
				appid = value.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return appid;
	}
	
	public static String getVersionCode() {
		String appid = "danjiddz";
		try {
			String packageName = OGMainActivity.getInstance().getPackageName();
			PackageManager mgr = OGMainActivity.getInstance().getPackageManager();
			Object value = mgr.getPackageInfo(packageName, 0).versionCode;
			if (value != null) {
				appid = value.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return appid;
	}
    
    public static void showMoreGame(String url){
    	OGMainActivity.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
//					ExchangeDataService es= new ExchangeDataService();
//					es.autofill = 0;
//					new ExchangeViewManager(OGMainActivity.getInstance(), es)
//					            .addView(ExchangeConstants.type_list_curtain, null, null);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
    }
    
    public static String getLocalIP() {
    	String tmpIP = "";
    	try {
    		tmpIP = OGSdkPlatform.getLocalIP();				    	
		} catch(Exception e) {
			e.printStackTrace();
		}
    	return tmpIP;
    }
    
    public static String getWriteLogFilesPath() {
    	String mPath = "";
    	try{
    		if(getSD()) {
    			String sSD = Environment.getExternalStorageDirectory().toString();
    			File filetemp = new File(sSD + "/download/");
    			if (!filetemp.exists()) {
    				filetemp.mkdir();
    			}
    			filetemp = new File(sSD + "/download/ourgame/");
    			if (!filetemp.exists()) {
    				filetemp.mkdir();
    			}
    			filetemp = new File(sSD + "/download/ourgame/log");
    			if (!filetemp.exists()) {
    				filetemp.mkdir();
    			}
    			mPath = sSD + "/download/ourgame/log/";     			
        	}else{
        		mPath = OGMainActivity.getInstance().getCacheDir().getAbsolutePath();
        	}
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	Log.v(LOGFILESPATH,LOGFILESPATH + "Path = "+ mPath);
    	return mPath;
    }
    
    public static void PromptTXT(final String showMessage) {
    	try {
			mHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					ClipboardManager clipboardManager = (ClipboardManager)OGMainActivity.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
					
					int sysVersion = Integer.parseInt(VERSION.SDK);  
					if(sysVersion>=11)
					{
						Toast.makeText(OGMainActivity.getInstance(), showMessage, Toast.LENGTH_SHORT).show(); 
					}
					else
					{
						Toast.makeText(OGMainActivity.getInstance(), showMessage, Toast.LENGTH_SHORT).show(); 
					}
				}
			}, 0);		
		} catch (Exception e) {
			e.printStackTrace();
		}    
	}
    
    //Copy 字串到  剪切板
	public static void CopyToClipBoard(final String strCopy,final String showMessage) {
		try {
			mHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					ClipboardManager clipboardManager = (ClipboardManager)OGMainActivity.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
					
					int sysVersion = Integer.parseInt(VERSION.SDK);  
					if(sysVersion>=11)
					{
						// TODO Auto-generated method stub
						clipboardManager.setPrimaryClip(ClipData.newPlainText(null, strCopy));
						if (clipboardManager.hasPrimaryClip()){
							clipboardManager.getPrimaryClip().getItemAt(0).getText();
						}
						Toast.makeText(OGMainActivity.getInstance(), showMessage, Toast.LENGTH_SHORT).show(); 
					}
					else
					{
						clipboardManager.setText(strCopy);  
						if (clipboardManager.hasText()){  
							clipboardManager.getText();  
						}
						Toast.makeText(OGMainActivity.getInstance(), showMessage, Toast.LENGTH_SHORT).show(); 
					}
				}
			}, 0);		
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public static void outLog(int level, final String logStr) {
		try {//暂时没有level区分统一用logv输出日志
			Log.v(DBTAG,logStr);
		}catch(Exception e) {
			e.printStackTrace();
		}	
	}
	
	private static Handler mHandler=new Handler(OGMainActivity.getInstance().getMainLooper());

	
	//打开Url
	public static void openBrowser(String url) {
		Uri uri = Uri.parse(url);
        OGMainActivity.getInstance().startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    public static int updateLB(boolean ISshow){
    	Log.i("LB", "start");
    	IQueryUpdateCallback callBack = new IQueryUpdateCallback() {
    	    public void onUpdateResult(int result) {
    	        Log.i("LB", "result="+result);
    	        UPDATE_TYPE = result;
    	    }
    	};
    	GameSdk.queryUpdate(OGMainActivity.getInstance(),callBack, true);
    	return UPDATE_TYPE;
    }
}