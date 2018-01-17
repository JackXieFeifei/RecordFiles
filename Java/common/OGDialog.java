
	
package com.og.common;


import com.og.danjiddz.R;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;




public class OGDialog extends Dialog {
	private Context context = null;
	private static OGDialog customProgressDialog = null;
	
	public OGDialog(Context context){
		super(context);
		this.context = context;
	}
	
	public OGDialog(Context context, int theme) {
        super(context, theme);
    }
	
	public static OGDialog createDialog(Context context){
		customProgressDialog = new OGDialog(context,R.style.CustomProgressDialog);
		customProgressDialog.setContentView(R.layout.ogprogressdialog);
		customProgressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
		return customProgressDialog;
	}

    public OGDialog setMessage(String strMessage){
    	TextView tvMsg = (TextView) customProgressDialog.findViewById(R.id.tv_tiptext);

    	if (tvMsg != null){
    		tvMsg.setText(strMessage);
    		tvMsg.getBackground().setAlpha(0);
    	}
    	
    	return customProgressDialog;
    }
}
