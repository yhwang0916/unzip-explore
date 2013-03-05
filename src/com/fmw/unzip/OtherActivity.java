package com.fmw.unzip;
import java.io.File;
import java.net.URI;


import com.box.unzip.R;
import com.fmw.unzip.ibz.IExtractFile;
import com.fmw.unzip.service.ExtractFile;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class OtherActivity extends Activity{
	private Context context = this;
	private ProgressDialog unzipProcess;
	private File operateFile;
	 private static final int DIALOG_PROCESS = 5;
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what){
			case 2:
				int unzipStatus = msg.getData().getInt("unzipCount");
				if(unzipStatus == -1){
					unzipProcess.dismiss();
					Toast.makeText(context, R.string.extract_fail, Toast.LENGTH_SHORT).show();
					//结束广告联盟
					//AppConnect.getInstance(context).finalize();
			    	OtherActivity.this.finish();
				}
				unzipProcess.setProgress(unzipStatus);
				if(unzipStatus==unzipProcess.getMax()){
			    	unzipProcess.dismiss();
			    	Toast.makeText(context, R.string.extract_complete, Toast.LENGTH_SHORT).show();
			    	//结束广告联盟
					//AppConnect.getInstance(context).finalize();
			    	OtherActivity.this.finish();
			    }
				break;
			
			default:
				
			}
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		operateFile = new File(URI.create(intent.getData().toString()));
		//Toast.makeText(context, file.getAbsolutePath(), 1).show();
		
		unzipProcess =(ProgressDialog) buildDialog(DIALOG_PROCESS);
		unzipProcess.show();
		if(operateFile.getAbsolutePath().toLowerCase().endsWith(".zip")){
			new Thread(new UnZipThread()).start();
		}else if(operateFile.getAbsolutePath().toLowerCase().endsWith(".rar")){
			new Thread(new UnRARThread()).start();
		}else if(operateFile.getAbsolutePath().toLowerCase().endsWith(".tar")){
			new Thread(new UnTarThread()).start();
		}
	}
	//zip解压子线程
	private class UnZipThread implements Runnable{

		@Override
		public void run() {
					
			ExtractFile extractFile = new ExtractFile(operateFile, null);
			extractFile.unZip(new IExtractFile() {
				
				@Override
				public void setMax(int max) {
					unzipProcess.setMax(max);
				}
				
				@Override
				public void onExtract(int i) {
					Message msg1 = new Message();
				    msg1.what = 2;
				    msg1.getData().putInt("unzipCount", i);
					handler.sendMessage(msg1);
				}
			});
			    
		}
		
		
	}
	
	/**
	 * 构建dialog
	 * @param id:dialog编号
	 * @return
	 */
	private Dialog buildDialog(int id){
		AlertDialog.Builder builder = new Builder(context);
		switch (id) {
		
		case DIALOG_PROCESS:
			  ProgressDialog dialog = new ProgressDialog(context);
			  dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			  return dialog;
			//break;
		
		default:
			break;
		}
		return builder.create();
	}
	
	//rar解压子线程
	private class UnRARThread implements Runnable{

		@Override
		public void run() {
			
			ExtractFile extractFile = new ExtractFile(operateFile,null);
			extractFile.unRAR(new IExtractFile() {
				
				@Override
				public void setMax(int max) {
					unzipProcess.setMax(max);
				}
				
				@Override
				public void onExtract(int i) {
					 Message msg1 = new Message();
					 msg1.what = 2;
					 msg1.getData().putInt("unzipCount", i);
					 handler.sendMessage(msg1);
				}
			});
			
			}
	}
	
	//Tar解压子线程
	private class UnTarThread implements Runnable{

		@Override
		public void run() {
			
			ExtractFile extractFile = new ExtractFile(operateFile,null);
			try {
				extractFile.unTar(new IExtractFile() {
					
					@Override
					public void setMax(int max) {
						unzipProcess.setMax(max);
					}
					
					@Override
					public void onExtract(int i) {
						 Message msg1 = new Message();
						 msg1.what = 2;
						 msg1.getData().putInt("unzipCount", i);
						 handler.sendMessage(msg1);
					}
				});
			} catch (Exception e) {
				Log.e("eee",e.toString());
			}
			
			}
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

	

