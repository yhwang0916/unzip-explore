package com.fmw.unzip;


import com.box.unzip.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.UMFeedbackService;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends Activity {
	private Context context = this;
	private long exitTime = 0;
	private final int DIALOG_ABOUT = 1;
	private final int DIALOG_HELP = 2;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);  
		setContentView(R.layout.setting);
		
		LinearLayout feedBackSetting = (LinearLayout)findViewById(R.id.setting_feedback);
		//LinearLayout myAppSetting = (LinearLayout)findViewById(R.id.setting_myapp);
		LinearLayout updateSetting = (LinearLayout)findViewById(R.id.setting_update);
		LinearLayout helpSetting = (LinearLayout)findViewById(R.id.setting_help);
		LinearLayout aboutSetting = (LinearLayout)findViewById(R.id.setting_about);
		ImageButton backButton = (ImageButton)findViewById(R.id.back);
		Button quitButton = (Button)findViewById(R.id.quit);
		quitButton.setOnClickListener(new MyClickListener());
		backButton.setOnClickListener(new MyClickListener());
		feedBackSetting.setOnClickListener(new MyClickListener());
		//myAppSetting.setOnClickListener(new MyClickListener());
		updateSetting.setOnClickListener(new MyClickListener());
		helpSetting.setOnClickListener(new MyClickListener());
		aboutSetting.setOnClickListener(new MyClickListener());
		
		
	}
	private class MyClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			switch (v.getId()){
			case R.id.setting_feedback:
				//用户反馈接口
				UMFeedbackService.openUmengFeedbackSDK(context);
				break;
			//case R.id.setting_myapp:
				//自家下载接口
				
				//break;
			case R.id.setting_help:
				showDialog(DIALOG_HELP);
				break;
			case R.id.setting_about:
				showDialog(DIALOG_ABOUT);
				break;
			case R.id.setting_update:
				//自动更新
				UmengUpdateAgent.update(context);
				UmengUpdateAgent.setUpdateAutoPopup(false);
				UmengUpdateAgent.setUpdateOnlyWifi(false);
				UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {

						@Override
						public void onUpdateReturned(int arg0,UpdateResponse arg1) {
							// TODO Auto-generated method stub
							 switch (arg0) {
					            case 0: // has update
					                UmengUpdateAgent.showUpdateDialog(context, arg1);
					                break;
					            case 1: // has no update
					                Toast.makeText(context, "已是最新版本", Toast.LENGTH_SHORT)
					                        .show();
					                break;
					            case 2: // none wifi
					            	UmengUpdateAgent.showUpdateDialog(context, arg1);
					                Toast.makeText(context, "no wifi", 500).show();
					            	break;
					            case 3: // time out
					                Toast.makeText(context, "网络超时", Toast.LENGTH_SHORT)
					                        .show();
					                break;
					            }
						}
				});
				break;
			case R.id.back:
				((Activity) context).finish();
				break;
			case R.id.quit:
				if(System.currentTimeMillis()-exitTime>2000){
					Toast.makeText(context, "再按一次退出程序", Toast.LENGTH_SHORT).show();
					exitTime = System.currentTimeMillis();
				}else{
					Intent intent = new Intent(Intent.ACTION_MAIN);  
		            intent.addCategory(Intent.CATEGORY_HOME);  
		            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  
		            startActivity(intent);  
		            android.os.Process.killProcess(android.os.Process.myPid());  
				}
				
				break;
			default:
				
			}
			
		}
		
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		return buildDialog(id);
	}

	private Dialog buildDialog(int id){
		AlertDialog.Builder builder = new Builder(context);
		switch (id) {
		case DIALOG_ABOUT:
			  builder.setTitle("解压文件管路 V1.2.1");
			  TextView textView = new TextView(context);
			  textView.setText("一款稳定、快速、高效的android解压工具，支持zip、rar、tar压缩格式，解压中文无乱码，集成文件管理功能，支持文件的复制、剪切、删除、重命名等常用文件管理功能。如果您在使用过程中遇到任何问题,请在\"用户反馈\"中反馈您的信息,让我们做的更好！\r\n\r\n联系作者 ：fmw530@163.com");
			  textView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			  builder.setView(textView);
			 
			break;
		
		case DIALOG_HELP:
			  builder.setTitle("帮助");
			  TextView textView2 = new TextView(context);
			  textView2.setText("解压：长按选择\"解压到当前目录\",或点击选择第三方解压程序\r\n" +
			  		"新建文件：点击menu按钮，选择新建文件\r\n"+
			  		"新建文件夹：点击menu按钮，选择新建文件夹\r\n"+
			  		"排序：点击menu按钮，选择排序r\n"+
			  		"删除：长按文件或文件夹选择删除\r\n"+
			  		"复制：长按文件或文件夹选择复制\r\n"+
			  		"剪切：长按文件或文件夹选择剪切,切换到目标目录选择粘贴\r\n"+
			  		"……\r\n\r\n"+
			  		"如果您有更多需求,请反馈您的意见");
			  textView2.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			  builder.setView(textView2);
			 
			break;
		}
		return builder.create();
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
	}
}
