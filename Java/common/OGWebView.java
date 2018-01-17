package com.og.common;

import java.util.Calendar;

import org.cocos2dx.lib.Cocos2dxGLSurfaceView;

import android.R.bool;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

@SuppressLint("SetJavaScriptEnabled")

public class OGWebView extends WebViewClient {
	private WebView mWebView;
	private int mWidth;
	private int mHeight;
	private ViewGroup mParent;
	private LinearLayout mLayout;
	private Context mContext;
	//private ProgressDialog mProgress = null;
	private long lastReqTime = 0;//上一次请求时间
	private final long MINREQDELAY = 3000;
	
	public static native boolean onRequestURL(String url);
	public OGWebView(Context contex) {

		super();
		lastReqTime = System.currentTimeMillis();
		mContext = contex;
	}
	
	public void initView(ViewGroup parent, int netcheck,int x, int y, int width, int height, String url) {
	    lastReqTime = System.currentTimeMillis();
		mWidth = width;
		mHeight = height;
		Context contex = mContext;
		mLayout = new LinearLayout(contex);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams (
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		mLayout.setLayoutParams(params);
		
		mParent = parent;
		mParent.addView(mLayout);
		
		params = new LinearLayout.LayoutParams (
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
			
		params.width = width;
		params.height = height;
		params.leftMargin = x;
		params.topMargin = y;

		mWebView = new WebView(contex);
		mWebView.setLayoutParams(params);
		mWebView.setBackgroundColor(0);
		mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setDomStorageEnabled(true); 
		mWebView.requestFocus();
		mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		
		mWebView.addJavascriptInterface(this, "JSBridge");
		mWebView.setWebViewClient(this);
		//mWebView.setFocusable(false);
		OGWebChromeClient chromeCLient = new OGWebChromeClient();
		mWebView.setWebChromeClient(chromeCLient);
//		android:imeOptions="flagNoExtractUi"
//				mWebView.
//		mWebView.setScrollContainer(true);
		//mWebView.resolveSizeAndState(size, measureSpec, childMeasuredState)
		lastReqTime = 0;//System.currentTimeMillis();
		if(netcheck==1){
			if(OGUtilities.isNetworkAvailable()){
				mWebView.loadUrl(url);
			}else{
				Toast toast = Toast.makeText(OGMainActivity.getInstance(), "您的网络不稳定，请检查网络连接！", Toast.LENGTH_SHORT); 
				toast.show();
			}
		}else{
				mWebView.loadUrl(url);
		}
		
       // Log.d("webview", "url="+url);
		mLayout.addView(mWebView);
	}
	
	public void initViewWithContent(ViewGroup parent, int netcheck,int x, int y, int width, int height, String content) {
	    lastReqTime = System.currentTimeMillis();
		mWidth = width;
		mHeight = height;
		Context contex = mContext;
		mLayout = new LinearLayout(contex);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams (
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		mLayout.setLayoutParams(params);
		
		mParent = parent;
		mParent.addView(mLayout);
		
		params = new LinearLayout.LayoutParams (
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
			
		params.width = width;
		params.height = height;
		params.leftMargin = x;
		params.topMargin = y;

		mWebView = new WebView(contex);
		mWebView.setLayoutParams(params);
		mWebView.setBackgroundColor(0);
		mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
		mWebView.getSettings().setDomStorageEnabled(true); 
		
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.requestFocus();
		mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		mWebView.addJavascriptInterface(this, "JSBridge");
		mWebView.setWebViewClient(this);
		//mWebView.setFocusable(false);
		OGWebChromeClient chromeCLient = new OGWebChromeClient();
		mWebView.setWebChromeClient(chromeCLient);
//		android:imeOptions="flagNoExtractUi"
//				mWebView.
//		mWebView.setScrollContainer(true);
		//mWebView.resolveSizeAndState(size, measureSpec, childMeasuredState)
		lastReqTime = 0;//System.currentTimeMillis();
		if(netcheck==1){
			if(OGUtilities.isNetworkAvailable()){
				mWebView.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
			}else{
				Toast toast = Toast.makeText(OGMainActivity.getInstance(), "您的网络不稳定，请检查网络连接！", Toast.LENGTH_SHORT); 
				toast.show();
			}
		}else{
			mWebView.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
		}
		
       // Log.d("webview", "url="+url);
		mLayout.addView(mWebView);
	}
	
	public void closeView() {
		if (mLayout != null && mWebView != null) {
			mWebView.clearFocus();
			Cocos2dxGLSurfaceView.getInstance().requestFocus();
			mLayout.removeView(mWebView);
			mParent.removeView(mLayout);
		}
		mWebView = null;
		mLayout = null;

	}
	
	public void reloadView() {
		if(mWebView!=null){
			mWebView.reload();
		}
	}
	
	public void setVisible(int flag) {
		Log.v("setVisible == ","setVisible =="+flag);
		mWebView.setVisibility(flag);
	}
	
	public void runJS(String js) {
		if(mWebView!=null){
			mWebView.loadUrl(js);
		}
	}
	public boolean canGobackTowebView() {
		return mWebView.canGoBack();
    }
    public void gobackTowebView() {
	mWebView.goBack();
    }
	public int getViewWidth() {
		return mWidth;
	}
	
	public int getViewHeight() {
		return mHeight;
	}
	
	public int  getClientPlatformType() {
		return 1;
	}

	public String  getMobileNum() {
		return OGUtilities.getMobileNum();
	}

	public String  getIMSI() {
		return OGUtilities.getMobileIMSI();
	}

	public String  getIMEI() {
		return OGUtilities.getMobileIMEI();
	}
	
	public boolean isNetworkAvailable() {
		return OGUtilities.isNetworkAvailable();
	}
	
	public void showEditBox() {
		OGMainActivity.getInstance().showEditBox();
	}
	
	public void showToast(String str) {
		Toast toast = Toast.makeText(OGMainActivity.getInstance(), str, Toast.LENGTH_SHORT); 
		toast.show();
	}
	
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, final String url) {
        if(OGUtilities.isNetworkAvailable()){
        	 if (url.startsWith("weixin://wap/pay?")) {
        		 try{
        			 Intent intent = new Intent();
                     intent.setAction(Intent.ACTION_VIEW);
                     intent.setData(Uri.parse(url));
                     OGMainActivity.getInstance().startActivity(intent);
        		 }catch(Exception p){
        			 
        		 }
                 
                 return true;
             }
            if(url!=null && (url.startsWith("ogshop://") ||  url.startsWith("ogact://") || url.startsWith("link://"))){
                if(System.currentTimeMillis()-lastReqTime > MINREQDELAY){
                    lastReqTime = System.currentTimeMillis();
                    return onRequestURL(url);
                }else{
                    return true;
                }
            }else{
                lastReqTime = System.currentTimeMillis();
                return  onRequestURL(url);
            }
        }else{
            Toast toast = Toast.makeText(OGMainActivity.getInstance(), "您的网络不稳定，请检查网络连接！", Toast.LENGTH_SHORT);
            toast.show();
            return  true;
        }
    }
	
	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
//		if (mProgress != null) {
//			mProgress.dismiss();
//		}
//		mProgress = ProgressDialog.show(mContext, "", "");
		OGMainActivity.getInstance().showSystemProgressDialog("请稍候...", OGMainActivity.Progress_Dialog, 30000);
		
    }
	
	@Override
	public void onPageFinished(WebView view, String url) {
		
		OGMainActivity.getInstance().hideSystemProgressDialog();
		
//		if (mProgress != null) {
//			mProgress.dismiss();
//		}
//		mProgress = null;
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
    	handler.proceed();
    }

}
