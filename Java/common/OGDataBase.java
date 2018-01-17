package com.og.common;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.og.danjiddz.R;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

class UserAccount {
	public String account;
	public String password;
	public int loginType;
}

class Payment {
	public String userName;
	public String statement;
	
	/**
	 * order's status
	 * 
	 * 0: wait for server ack
	 * 5: user have paid & order haven't be created 
	 */
	public int status;
}

class DataBaseHelper extends SQLiteOpenHelper {
	public DataBaseHelper(Context context, String name, int version) {
		super(context, name, null, version);
		//Log.i("", "/--> DataBaseHelper.name="+name+"/"+version+"/"+context);
	}
	
	public void onCreate(SQLiteDatabase paramSQLiteDatabase) {
	}
	
	public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2) {
	}
}

/**
 * 数据库信息
 * 
 * 总共个表：设置、用户信息、订购信息
 */
public class OGDataBase {
	/**
	 * 用户表 T_USER[17]：[1]账号、密码、角色名、邮箱、性别、[6]金币、等级、经验值、胜场、负场、[11]游戏时长(秒)、登录渠道、
	 * 最后登录时间、上次游戏房间id、上次游戏ip、[16]上次游戏的端口、上次游戏时间、上次的房间等级、免充次数。//最后4各变量针对断线续玩 设置表
	 * 
	 * T_SETTING[8]：声音开关、音量大小、背景音乐、振动、出牌顺序、自动开始、显示页面、模块。
	 *  
	 * T_PAYMENT：订单、账号、状态、支付时间、到帐时间、
	 * 昵称、支付类型、PayInfo1、PayInfo2、PayInfo3、token、ChannelID。 {订购信息表 Status "0"等待
	 * "1"成功 "2"已提示 "3"卡密错误或者面额不符 4元宝不足或者未知错误} 任务
	 * 
	 * T_HONOUR[11]：[1]编号、名称、描述、状态（0-未获得，1-获得）、[5]完成时间
	 * 、目标值、进度、奖励金币、奖励经验、用户名、用户来源。
	 */
	public static final int LOGIN_GATE_OFFLINE = -1;		//单机
	public static final int LOGIN_GATE_LIANZHONG = 0;		//联众
	public static final int LOGIN_GATE_OPENID_RENREN = 1;	//人人

	private static Context mContext = null;
	private static DataBaseHelper mDBHelper = null;
	private static SQLiteDatabase mDatabase = null;
	
	private static DataBaseHelper mDBCombatHelper = null;
	private static SQLiteDatabase mDatabaseCombat = null;

	/**
	 * 设置数据
	 */
	public static int miSound; // 声音开关
	public static int miVolume; // 音量——范围0-100
	public static int miMusic; // 背景音乐
	public static int miVibrate; // 振动
	
	/**
	 * 用户登录数据
	 */
	public static ArrayList<UserAccount> mUserList;

	/**
	 * 单机金币数据
	 */
	public static Long mlSingleCoin;	// 单机金币
	
	public static int miExperience;		// 经验值
	public static int miWin;			// 胜场
	public static int miDefeat;			// 负场
	
	/**
	 * 单机荣誉数据
	 */
	public static ArrayList<Integer> mHonorList = null;
	
	/**
	 * 未完成的订单
	 */
	public static Payment mPayment = null;
	
	//------------------------------------
	//	final
	//------------------------------------
//	private static final String SQL_CREATE_COMBAT = "CREATE TABLE IF NOT EXISTS T_STAGE(Key text, Val text, Des Integer, primary key(Key));";
	private static final String SQL_CREATE_COMBAT = "CREATE TABLE IF NOT EXISTS T_STAGE(Key text, Val BLOB, Des Integer, primary key(Key));";
	
	private static final String DATABASE_NAME = "ddz.combat";
	private static String m_sOldDB_Name = "ddz.LandLords";
	
	private static final String TAG = "OGCombatDataBase";
	private static final String ES = "es";
	
	
	
	
	public static void initDataBase(Context context) {
		//Log.i(TAG, "/--> initDataBase");
		
		mContext = context;
		mDBHelper = new DataBaseHelper(mContext, m_sOldDB_Name, 1);
		mDatabase = mDBHelper.getReadableDatabase();
		mHonorList = new ArrayList<Integer>();
		mUserList = new ArrayList<UserAccount>();
	}
	
	public static void closeDataBase() {
		mDatabase.close();
		mDBHelper.close();
		mHonorList.clear();
		mUserList.clear();
		
		File f = new File(mContext.getApplicationInfo().dataDir + "/databases/" + m_sOldDB_Name);
		//Log.i(TAG, "closeDataBase"+f.exists());
		if (f.exists()){
			f.delete();
		}
		mContext = null;
	}
	
	public static void closeDataBaseCombat() {
		mDatabaseCombat.close();
		mDBCombatHelper.close();
	}

	/**
	 * 从老的数据库中读取数据
	 * 
	 * @return 返回数据库是否存在
	 */
	public static boolean importData() {
		try {
			if (isDataBaseExist() && mDatabase != null) {
				importUser();
				importSettings();
				importHonor();
				importPayment();
				return true;
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
		return false;
	}
	
	/**
	 * 将数据导出到json串
	 * @throws JSONException 
	 */
	public static String exportToJson() throws JSONException {
		
		JSONObject json = new JSONObject();
		
		// 单机金币
		json.put("singleCoin", mlSingleCoin);
		// 单机经验
		json.put("experience", miExperience);
		// 单机胜
		json.put("win", miWin);
		// 单机负
		json.put("defeat", miDefeat);
		
		// 登录信息
		JSONArray userList = new JSONArray();
		for (int i = 0; i < mUserList.size(); ++i) {
			UserAccount user = mUserList.get(i);
			JSONObject userJson = new JSONObject();
			userJson.put("account", user.account);
			userJson.put("password", user.password);
			userJson.put("loginType", user.loginType);
			//userList.add(user);
			userList.put(user);
		}
		json.put("user", userList);
		
		// 设置
		JSONObject settings = new JSONObject();
		settings.put("volume", miVolume);
		settings.put("mute", miSound);
		settings.put("music", miMusic);
		json.put("settings", settings);
		
		// 荣誉
		JSONArray honorList = new JSONArray();
		for (int i = 0; i < mHonorList.size(); i++) {
			Integer progress = mHonorList.get(i);
			honorList.put(progress);
		}
		json.put("honor", honorList);
		
		// 订单
		JSONObject payment = new JSONObject();
		if (mPayment != null) {
			payment.put("userName", mPayment.userName);
			payment.put("statement", mPayment.statement);
			payment.put("stats", mPayment.status);
			json.put("payment", payment);
		}
		
		return json.toString();
	}
	
	/**
	 * 是否存在数据库
	 */
	public static boolean isDataBaseExist() {
		File f = new File(OGMainActivity.getInstance().getApplicationInfo().dataDir + "/databases/" + m_sOldDB_Name);
		//Log.i(TAG, "[isDataBaseExist]="+OGMainActivity.getInstance().getApplicationInfo().dataDir);
		return f.exists();
	}

	/**
	 * 导入用户数据
	 */
	private static void importUser() {
		try {
			// 用户表
			String[] arrayOfString = new String[7];

			arrayOfString[0] = "UserName";
			arrayOfString[1] = "PassWord";
			arrayOfString[2] = "LoginGate";
			
			arrayOfString[3] = "Coins";
			arrayOfString[4] = "Experience";
			arrayOfString[5] = "Win";
			arrayOfString[6] = "Defeat";
			
			Cursor localCursor = mDatabase.query("T_USER", arrayOfString, null, null,
					null, null, " LastDate Desc");
			localCursor.moveToFirst();
			//Log.i(TAG, "/--> importUser="+localCursor.getCount());

			while (localCursor.getCount() > 0) {
				String userName = localCursor.getString(0);
				String password = localCursor.getString(1);
				int loginGate = localCursor.getInt(2);
				
				if (loginGate == LOGIN_GATE_OFFLINE) {
					mlSingleCoin = localCursor.getLong(3);
					miExperience = localCursor.getInt(4);
					miWin = localCursor.getInt(5);
					miDefeat = localCursor.getInt(6);
				}
				else
				{
					UserAccount user = new UserAccount();
					user.account = userName;
					user.password = password;
					user.loginType = loginGate;
					
					mUserList.add(user);
				}
				
				if (!localCursor.moveToNext()) {
					break;
				}
			}
			
			localCursor.close();
		} catch (Exception ex) {
			//Log.i(TAG, "/--> importUser.exception"+ex.toString());
		}
	}
	
	/**
	 * 导入设置数据
	 */
	private static void importSettings(){
		try {
			Cursor localCursor = mDatabase.query("T_SETTING",
					null, null, null, null, null, null);
			localCursor.moveToFirst();
			miSound 			= localCursor.getInt(0);
			miVolume 			= localCursor.getInt(1);
			miMusic 			= localCursor.getInt(2);
			miVibrate 			= localCursor.getInt(3);
			localCursor.close();
		} catch (Exception localException) {
		}
	}
	
	/**
	 * 导入单机荣誉数据
	 */
	private static void importHonor() {
		try {
			String[] arrayOfString = new String[2];
			arrayOfString[0] = "id";
			arrayOfString[1] = "Degree";

			Cursor localCursor = mDatabase.query("T_HONOUR",
					arrayOfString, null, null, null, null, "id asc");
			localCursor.moveToFirst();

			//Log.i(TAG, "/--> importHonor"+localCursor.getCount());
			while (localCursor.getCount() > 0) {
				int degree = localCursor.getInt(1);
				mHonorList.add(degree);
				
				if (!localCursor.moveToNext()) {
					break;
				}
			}
			localCursor.close();
		} catch (Exception localException) {
			//Log.i(TAG, "/--> importHonor.exception"+localException.toString());
		}
	}

	/**
	 * 导入充值后未到帐的账单
	 */
	public static void importPayment() {
		try {
			String sql = "select statement,userName,status,payDate,CoinComeDate from "
					+ "T_PAYMENT"
					+ " where status = 0 or status = 5" 
					+ " order by payDate desc";

			Cursor localCursor = mDatabase.rawQuery(sql, null);
			localCursor.moveToFirst();

			if (localCursor.getCount() > 0) {
				mPayment = new Payment();
				mPayment.statement = localCursor.getString(0);
				mPayment.userName = localCursor.getString(1);
				mPayment.status = localCursor.getInt(2);
			}
			localCursor.close();
		} catch (Exception localException) {
		}
	}

	/**
	 * set value
	 * 
	 * @param key
	 * @param value
	 * @param des 		encryption : !=0
	 * @return true:success
	 */
	public static boolean setValue(String key, byte[] str, int des){
		boolean bRet = false;
		
		try{
			key += ES;
			//Log.i(TAG, "/--> setValue.key="+key+"/"+des);
			if (initCombatDB()){
				if (mDatabaseCombat.isOpen()){
					String sql = "select Val from "
							+ "T_STAGE"
							+ " where Key = '" + key +"'";
					//Log.i(TAG, "/--> setValue.sql="+sql);
					Cursor localCursor = mDatabaseCombat.rawQuery(sql, null);
					localCursor.moveToFirst();
					
					int count = localCursor.getCount();
					localCursor.close();
					
					//Log.i(TAG, "/--> setValue.count="+count);
					if (count > 0){
						ContentValues args = new ContentValues();
						args.put("Val", str);
						args.put("Des", des);
						mDatabaseCombat.update("T_STAGE", args, "Key='"
								+ key + "'", null);
					}else{
						ContentValues localContentValues = new ContentValues();
						localContentValues.put("Key", key);
						localContentValues.put("Val", str);
						localContentValues.put("Des", des);

						mDatabaseCombat.insert("T_STAGE", null, localContentValues);
					}
					
					mDatabaseCombat.close();
					bRet = true;
				}
			}
		}catch(Exception e){
		//	Log.i(TAG, "[DB.setValue].err="+e.toString());
		}
		return bRet;
	}
	
	/**
	 * get value
	 * 
	 * @param key
	 * @return value
	 */
	public static byte[] getValue(String key){
		String sRet = null;
		
		//Log.i(TAG, "/--> getValue.key="+key);
		
		key += ES;
		
		byte[] bt = null;
		byte[] btRet = null;
		
		if (initCombatDB()){
			if (mDatabaseCombat.isOpen()){
				String[] arrayOfString = new String[2];
				arrayOfString[0] = "Des";
				arrayOfString[1] = "Val";
				
				String sql = "Key = '" + key + "'";
				Cursor localCursor = mDatabaseCombat.query("T_STAGE",
						arrayOfString, sql, null, null, null, null);
				localCursor.moveToFirst();
				
				//Log.i(TAG, "/--> sql="+sql);
				
				//Log.i(TAG, "/--> getValue.count="+localCursor.getCount());
				
				if (localCursor.getCount() > 0){
					sRet = String.valueOf(localCursor.getInt(0));
					
					bt = localCursor.getBlob(1);
					
					btRet = new byte[bt.length + 1];
					btRet[0] = sRet.getBytes()[0];
					System.arraycopy(bt, 0, btRet, 1, bt.length);
					//Log.i(TAG, "/--> getValue.sRet="+sRet+"/"+bt.length);
				}
				localCursor.close();
				
				//Log.i(TAG, "/--> getValue.sRet="+sRet);
				
				mDatabaseCombat.close();
			}
		}
		
		return btRet;
	}
	
	private static boolean initCombatDB(){
		boolean bRet = false;
		
		try {
			if (mDatabaseCombat == null || !mDatabaseCombat.isOpen()) {
				if (mDBCombatHelper == null) {
					mDBCombatHelper = new DataBaseHelper(
							OGMainActivity.getInstance(), DATABASE_NAME, 1);
				}
				mDatabaseCombat = mDBCombatHelper.getWritableDatabase();
				while (mDatabaseCombat.isDbLockedByCurrentThread()){  
			        //Log.w(TAG, "insert === db is locked by other or current threads!");  
			        try {  
			            Thread.sleep(20);  
			        } catch (InterruptedException e) {  
			            e.printStackTrace();  
			        }  
			    }
				mDatabaseCombat.execSQL(SQL_CREATE_COMBAT);
				bRet = true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return bRet;
	}
}
