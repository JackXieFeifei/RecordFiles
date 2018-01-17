package com.og.common;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import com.og.danjiddz.R;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.util.Log;

public class OGDownloadAPK {
	
	private File mSaveFile;			// 下载文件
	private String msUrl;			// apk位置
	public static URL mDownUrl;	// 下载链接
	private int miFileLength;		// 文件长度
	private int miDownLength = 0;	// 当前下载的多少
	private static boolean mbShowProgress;	// 进度
	
	private int mErrCode;
	
	private static final String TAG = "OGDownloadAPK";
	
	public static final String APKNAME = "landlord-android.apk";//名称
	
	private static native void showInstallWnd();
	private static native void nativeUpdateProgress(int progress);
	private static native void nativeFaildReason(String sReason);
	
	public OGDownloadAPK(String url){
		msUrl = url;
	}
	
	public void run() {
		Log.i(TAG, "OGDownloadAPK -----[run]");
		
		new Thread(new Runnable() {
			public void run() {
				if (getLength(msUrl)){
					try {
						if (mDownUrl == null){
							mDownUrl = new URL(msUrl);
							
							createFile();
						}
						
						HttpURLConnection http = (HttpURLConnection) mDownUrl
								.openConnection();
						http.setConnectTimeout(5 * 1000);
						http.setRequestMethod("GET");
						http.setRequestProperty(
								"Accept",
								"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
						http.setRequestProperty("Accept-Language", "zh-CN");
						http.setRequestProperty("Referer", mDownUrl.toString());
						http.setRequestProperty("Charset", "UTF-8");
						http.setRequestProperty("Range", "bytes=" + 0 + "-" + miFileLength);
						http.setRequestProperty(
								"User-Agent",
								"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
						http.setRequestProperty("Connection", "Keep-Alive");
						InputStream inStream = http.getInputStream();
						byte[] buffer = new byte[1024];
						int offset = 0;
						RandomAccessFile threadfile = new RandomAccessFile(mSaveFile, "rw");
						threadfile.seek(0);
						while ((offset = inStream.read(buffer, 0, 1024)) != -1) {
							threadfile.write(buffer, 0, offset);
							miDownLength += offset;
							
							// 通知UI刷新进度条
							if (mbShowProgress){
								OGMainActivity.getInstance().runOnGLThread(new Runnable() {
						            @Override
						            public void run() {
						            	int n = (miFileLength == 0) ? 0 : (miDownLength * 100 / miFileLength);
//						            	nativeUpdateProgress(n);
						            }
								});
							}
						}
						threadfile.close();
						inStream.close();
						http.disconnect();
						
						Log.i(TAG, "[miDownLength]="+miDownLength+"/"+miFileLength);
						if (miDownLength == miFileLength){
							intallAPK();
						}
					} catch (Exception e) {
						//提示——下载的过程中出现异常
						onDownloadFaild(OGMainActivity.getInstance().getString(R.string.error_tip_download_faild));
					}
				}else{
					Log.e(TAG, "[run].fileLength = 0");
					if (mErrCode == 1){
						onDownloadFaild(OGMainActivity.getInstance().getString(R.string.error_tip_no_capacity));
					}else{
						onDownloadFaild(OGMainActivity.getInstance().getString(R.string.error_tip_download_faild));
					}
				}
			}
		}).start();
	}
	
	/**
	 * 获得下载文件大小
	 */
	private boolean getLength(String downloadUrl) {
		boolean bRet = false;
		mErrCode = 0;
		
		try {
			URL url = new URL(downloadUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5 * 1000);
			if (conn.getResponseCode() == 200) {
				miFileLength = conn.getContentLength();
			}
			conn.disconnect();
			//Log.i("haha1", miFileLength+"/"+OGUtilities.getSDCapacity());
			long len = OGUtilities.getSDCapacity();
			bRet = (miFileLength > 0 && miFileLength < len);
			if (!bRet){
				mErrCode = 1;
			}
		} catch (Exception e) {
			mErrCode = 2;
		}
		
		return bRet;
	}
	
	/**
	 * 创建下载文件
	 */
	private void createFile(){
		String sSD = Environment.getExternalStorageDirectory().toString();
		File filetemp = new File(sSD + "/download/");
		if (!filetemp.exists()) {
			filetemp.mkdir();
		}
		filetemp = new File(sSD + "/download/ourgame/");
		if (!filetemp.exists()) {
			filetemp.mkdir();
		}
		mSaveFile = new File(sSD + "/download/ourgame/", APKNAME);

		if (!mSaveFile.exists()) {
			filetemp.mkdir();
		}
		else{
			mSaveFile.delete();
		}
	}
	
	/**
	 * 请求安装游戏
	 */
	private void reqInstallApp(){
		//Log.i(TAG, "[installApp]");
		
		OGMainActivity.getInstance().runOnGLThread(new Runnable() {
            @Override
            public void run() {
//            	showInstallWnd();
            }
		});
	}
	
	public static void intallAPK(){
		OGMainActivity.getInstance().runOnGLThread(new Runnable() {
            @Override
            public void run() {
//            	showInstallWnd();
            }
		});
		OGMainActivity.getInstance().startActivity(getIntent(mDownUrl.toString()));
	}
	
	private static Intent getIntent(String urlStr){
		String fileName = Environment.getExternalStorageDirectory().toString() + "/download/ourgame/" + APKNAME;
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(fileName)),
				"application/vnd.android.package-archive");
		
		return intent;
	}
	
	/**
	 * 卸载应用程序
	 * 
	 * 下载包和当前包不同，不能覆盖
	 */
	private void unloadApp(){
		//Log.i(TAG, "[unloadApp]");
	}
	
	/**
	 * 显示下载进度
	 */
	public void onShowProgress()
	{
		mbShowProgress = true;
		
		if (miFileLength != miDownLength){
			OGMainActivity.getInstance().runOnGLThread(new Runnable() {
	            @Override
	            public void run() {
	            	int n = (miFileLength == 0) ? 0 : (miDownLength * 100 / miFileLength);
//	            	nativeUpdateProgress(n);
	            }
			});
		}else{
//			this.reqInstallApp();
			intallAPK();
		}
	}
	
	/**
	 * 更新失败
	 * @param sReason
	 */
	private void onDownloadFaild(final String sReason){
		OGMainActivity.getInstance().runOnGLThread(new Runnable() {
            @Override
            public void run() {
//            	nativeFaildReason(sReason);
            }
		});
		// 释放对象
		Message msg = new Message();
		msg.what = OGMainActivity.HANDLER_DOWNLOAD_CLOSE;
		OGMainActivity.getInstance().mHandler.sendMessage(msg);
	}
	
	/**
	 * 释放
	 */
	public void release(){
		mSaveFile = null;
		mDownUrl = null;
	}
	
	/**
	 * 下载框关闭
	 */
	public static void onDownloadWndClose(){
		mbShowProgress = false;
	}
}
