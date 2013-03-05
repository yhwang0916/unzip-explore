package com.fmw.unzip;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.box.unzip.R;
import com.fmw.unzip.ibz.IExtractFile;
import com.fmw.unzip.service.ExtractFile;
import com.fmw.unzip.service.MyAdapter;
import com.fmw.unzip.tools.Tools;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.NotificationType;
import com.umeng.fb.UMFeedbackService;
import com.umeng.update.UmengUpdateAgent;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity {
	private Context context = this;
	private List<HashMap<String, Object>> dataList;
	private File file;
	private File operateFile;
	private String oprateFile_flag;
	private String createFile_flag;
	private String sortOption = "type";
	private TextView pathView;
	private ImageView backButton;
	private EditText newFileName;
	private long exitTime = 0;
	private boolean showHide = false;
	private static final int ITEM_NEW = Menu.FIRST;
    private static final int ITEM_DEL = Menu.FIRST+1;
    private static final int ITEM_RENAME = Menu.FIRST+2;
    private static final int ITEM_COPY = Menu.FIRST+3;
    private static final int ITEM_CUT = Menu.FIRST+4;
    private static final int ITEM_UNZIP = Menu.FIRST+5;
    private static final int ITEM_CHOISER = Menu.FIRST+6;
    
    private static final int DIALOG_QUIT = 1;
    private static final int DIALOG_RENAME = 2;
    private static final int DIALOG_DELETE = 3;
    private static final int DIALOG_CREATE = 4;
    private static final int DIALOG_PROCESS = 5;
    private static final int DIALOG_CHOUSER = 6;
    private ListView listView ;
    private LinearLayout optionLinearLayout ;
    private Button pasteButton ;
    private Button cpasteButton;
    private ProgressDialog unzipProcess;
    private ProgressDialog deleteProgress;
    private ProgressDialog copyProgress;
    //子线程信息传输通道
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what){
			case 0:
				String path = msg.getData().getString("path");
				file = new File(path);
				show(file);
				super.handleMessage(msg);
				break;
			case 1:
				unzipProcess.setMax(msg.getData().getInt("unzipMax"));
				unzipProcess.setCancelable(false);
				unzipProcess.show();
				break;
			case 2:
				int unzipStatus = msg.getData().getInt("unzipCount");
				if(unzipStatus == -1){
					unzipProcess.dismiss();
					Toast.makeText(context, R.string.extract_fail, Toast.LENGTH_SHORT).show();
				}
				unzipProcess.setProgress(unzipStatus);
				if(unzipStatus==unzipProcess.getMax()){
			    	show(file);
			    	unzipProcess.dismiss();
			    	Toast.makeText(context, R.string.extract_complete, Toast.LENGTH_SHORT).show();
			    }
				break;
			case 3:
				if(msg.getData().getBoolean("delete")){
					deleteProgress.dismiss();
					show(file);
					Toast.makeText(MainActivity.this,R.string.success, 1).show();
				}else{
					deleteProgress.dismiss();
					Toast.makeText(MainActivity.this,R.string.fail, 1).show();
				}
				break;
			case 4:
				copyProgress.dismiss();
				if(msg.getData().getBoolean("copyFlag")){
					Toast.makeText(MainActivity.this, R.string.success, 1).show();
					show(file);
				}else{
					Toast.makeText(MainActivity.this, R.string.fail, 1).show();
				}
				break;
			default:
				
			}
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//友盟错误返回
		MobclickAgent.onError(this);
		//友盟反馈回复提醒
		UMFeedbackService.enableNewReplyNotification(this, NotificationType.AlertDialog);
		//友盟自动更新
		UmengUpdateAgent.setUpdateOnlyWifi(false);
		UmengUpdateAgent.update(context);
		UmengUpdateAgent.setUpdateAutoPopup(true);
		
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		//注册返回点击事件
		backButton = (ImageView)findViewById(R.id.back);
		backButton.setOnClickListener(new ButtonClickListener());
		TextView current_dirView = (TextView)findViewById(R.id.current_dir);
		ImageButton settingButton = (ImageButton)findViewById(R.id.setting);
		settingButton.setOnClickListener(new ButtonClickListener());
		current_dirView.setOnClickListener(new ButtonClickListener());
		//settingButton.setOnTouchListener(new ButtonTouchListener());
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED)){
			ImageView listEmpty = (ImageView)(findViewById(R.id.list_empty));
			listEmpty.setImageResource(R.drawable.nosdcard);
			listEmpty.setAlpha(30);
			file = null;
			return;
		}
		file = Environment.getExternalStorageDirectory();
		show(file);
		
		
		
		//显示当前目录
		pathView =(TextView) findViewById(R.id.current_dir);
		//获取copy 选择操作布局
		optionLinearLayout =(LinearLayout) findViewById(R.id.option);
		pasteButton= (Button)findViewById(R.id.paste);
		pasteButton.setOnClickListener(new ButtonClickListener());
		cpasteButton = (Button)findViewById(R.id.cancel_paste);
		cpasteButton.setOnClickListener(new ButtonClickListener());
		
		pathView.setText(Tools.shortPath(file.getAbsolutePath()));
		//注册上下文事件
		registerForContextMenu(pathView);
		
		
	}

	//界面ui
	private void show(File file) {
		
		listView = (ListView)findViewById(R.id.listView);
		listView.setBackgroundDrawable(null);
		dataList = new ArrayList<HashMap<String,Object>>();
		HashMap<String, Object> item = new HashMap<String, Object>();
		item.put("item_icon", "icon");
		item.put("item_name", "filename");
		item.put("item_size", "filesize");
		dataList.add(item);
		//获取指定路径list
		MyAdapter adapter =null;
		new Tools(context);
		dataList = Tools.getFileList(file,showHide,context);
		if(dataList==null|| dataList.isEmpty()){
			List<HashMap<String, Object>> datalList2 = new ArrayList<HashMap<String,Object>>();
			HashMap<String, Object> item2 = new HashMap<String, Object>();
			item2.put("bg_img", null);
			datalList2.add(item2);
			
			MyAdapter adapter2 = new MyAdapter(context, datalList2, R.layout.emptyitem, new String[]{"bg_img"}, new int[]{R.id.item_empty});
			listView.setAdapter(adapter2);
			ImageView listEmpty = (ImageView)(findViewById(R.id.list_empty));
			listEmpty.setImageResource(R.drawable.bg_empty);
			//listView.setBackgroundResource(R.drawable.bg_empty);
		}else{
			ImageView listEmpty = (ImageView)(findViewById(R.id.list_empty));
			listEmpty.setImageResource(0);
			//排序
			dataList = Tools.sortList(dataList, sortOption);
			adapter = new MyAdapter(this, dataList,R.layout.item,
					new String[]{"item_img","item_name","item_time","item_size"}, 
					new int[]{R.id.item_img,R.id.item_name,R.id.item_time,R.id.item_size});
			listView.setAdapter(adapter);
			
		}
		    
		
		//注册点击事件
		//listView.setOnItemLongClickListener(new OnItemLongClick());
		listView.setOnItemClickListener(new OnItemClick());
		registerForContextMenu(listView);
	}

	//Menu事件
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if(file == null){
			return false;
		}
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	
	@SuppressLint("NewApi")
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.new_fold:
			createFile_flag = "fold";
			operateFile = file;
			showDialog(DIALOG_CREATE);
			break;
		case R.id.new_file:
			createFile_flag = "file";
			operateFile = file;
			showDialog(DIALOG_CREATE);
			break;
		case R.id.sort:
			
			break;
		case R.id.sort_name:
			sortOption = "name";
			show(file);
			break;
		case R.id.sort_size:
			sortOption = "size";
			show(file);
			break;
		case R.id.sort_time:
			sortOption = "time";
			show(file);
			break;
		case R.id.sort_type:
			sortOption = "type";
			show(file);
			break;

		case R.id.show_hide:
			if(!showHide){
				showHide = true;
				item.setTitle(R.string.unshow_hide);
			}else{
				showHide = false;
				item.setTitle(R.string.show_hide);
			}
			show(file);
			break;
		case R.id.refresh:
			show(file);
			break;
		case R.id.exit:
			 finish();
	         System.exit(0);
			break;
		default:
			break;
		}
		
		
		
		return super.onMenuItemSelected(featureId, item);
	}


	//item点击事件类
	private class OnItemClick implements OnItemClickListener {

		
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {

			ListView lView = (ListView)arg0;
			HashMap<String, Object> item;
			if((lView.getItemAtPosition(arg2)) instanceof HashMap<?, ?>){
				item = (HashMap<String, Object>)(lView.getItemAtPosition(arg2));
			}else{
				return;
			}
			
			String path = Environment.getExternalStorageDirectory().toString();
			if(item.get("item_path")!=null){
				path = item.get("item_path").toString();
			}else{
				return;
			}
			
			
			if(new File(path).isFile()){
				operateFile = new File(path);
				String type = item.get("item_type").toString();	
				if(type=="zzz"||type==null||type.length()==0){
		        	  showDialog(DIALOG_CHOUSER);
		        	  return;
		          }
				Intent intent = new Intent(Intent.ACTION_VIEW);
		           intent.addCategory("android.intent.category.DEFAULT");
		           intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		           Uri uri = Uri.fromFile(new File(path));
		           intent.setDataAndType(uri, type);

		          startActivity(Intent.createChooser(intent, getString(R.string.select_option)));
		           //Toast.makeText(MainActivity.this, type, 1).show();
				 return;
			}
			pathView.setText(Tools.shortPath(path));
			//用bundler给主进程传递参数
			Message msg = new Message();
			msg.what = 0;
			msg.getData().putString("path", path);
			handler.sendMessage(msg);
		}
		
	}
	
	//item长点击事件
	private class OnItemLongClick implements OnItemLongClickListener{
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			 //dialog();
			
			return false;
		}
		
		
	}
	
	//上下文菜单（长按菜单）
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo menuInfo1 = (AdapterContextMenuInfo)menuInfo;
		if(menuInfo1 == null||listView==null){
			return;
		}
		HashMap<String, Object> itemx;
		if((listView.getItemAtPosition(menuInfo1.position)) instanceof HashMap<?, ?>){
			itemx = (HashMap<String, Object>)listView.getItemAtPosition(menuInfo1.position);
		}else{
			return;
		}
		
		String path = Environment.getExternalStorageDirectory().toString();
		if(itemx.get("item_path")!=null){
			operateFile = new File(itemx.get("item_path").toString());
		}else{
			return;
		}
		String mimeType = Tools.getMimeType(operateFile.getAbsolutePath());
		String fileNameExt = operateFile.getAbsolutePath().substring(operateFile.getAbsolutePath().length()-3).toLowerCase();
		menu.setHeaderTitle(R.string.select_option);
        //添加菜单项
		if("application/x-gzip".equals(mimeType)||"application/octet-stream".equals(mimeType)||"zip".equals(fileNameExt)||"rar".equals(fileNameExt)||"tar".equals(fileNameExt)){
        	menu.add(0, ITEM_UNZIP, 0, R.string.extract_to_this_dir);
        }
		if("000DIR".equals(mimeType)){
			menu.add(0, ITEM_NEW, 0, R.string.new_child_fold);
		}
		menu.add(0, ITEM_CHOISER, 0, R.string.open_option);
		menu.add(0, ITEM_RENAME, 0, R.string.rename); 
        menu.add(0, ITEM_COPY, 0, R.string.copy);
        menu.add(0, ITEM_CUT, 0, R.string.cut);
        menu.add(0, ITEM_DEL, 0, R.string.delete);               

		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@SuppressLint("NewApi")
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		boolean b = false;
		
		switch(item.getItemId()){
		case ITEM_NEW:
			createFile_flag = "fold";
			showDialog(DIALOG_CREATE);
			break;
		case ITEM_DEL:
			showDialog(DIALOG_DELETE);
			break;
		case ITEM_RENAME:
			if(newFileName!=null){
				newFileName.setText(operateFile.getName());
			}
			showDialog(DIALOG_RENAME);
			break;
		case ITEM_COPY:
			optionLinearLayout.setVisibility(View.VISIBLE);
			oprateFile_flag = "copy";
			break;
		case ITEM_CUT:
			optionLinearLayout.setVisibility(View.VISIBLE);
			oprateFile_flag = "cut";
			break;
		case ITEM_UNZIP:
			/**
			 * 
			 * 进度条为实现*********************************
			 * 
			 */
			unzipProcess =(ProgressDialog) buildDialog(DIALOG_PROCESS);
			unzipProcess.show();
			if(operateFile.getAbsolutePath().toLowerCase().endsWith(".zip")){
				new Thread(new UnZipThread()).start();
			}else if(operateFile.getAbsolutePath().toLowerCase().endsWith(".rar")){
				new Thread(new UnRARThread()).start();
			}else if(operateFile.getAbsolutePath().toLowerCase().endsWith(".tar")){
				new Thread(new UnTarThread()).start();
			}
			
			break;
		case ITEM_CHOISER:
			Uri uri = Uri.fromFile(operateFile);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(uri, "*/*");
			startActivity(Intent.createChooser(intent, getString(R.string.select_option)));
			break;
		default:
		}
		
		return super.onContextItemSelected(item);
	}

	//键盘点击事件
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if(keyCode == KeyEvent.KEYCODE_BACK){
			
			
			if(file == null ||file.getAbsoluteFile().equals( Environment.getExternalStorageDirectory().getAbsoluteFile())){
				//showDialog(DIALOG_QUIT);
				this.quit();
				return false;
			}
			file = file.getParentFile();
			//用bundler给主进程传递参数
			pathView.setText(Tools.shortPath(file.getAbsolutePath()));
			Message msg = new Message();
			msg.getData().putString("path", file.getAbsolutePath());
			handler.sendMessage(msg);
			return true;
		}
		return super.onKeyDown(keyCode, event);
		
	}
	private class ButtonTouchListener implements OnTouchListener{

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (v.getId()) {
			case R.id.setting:
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					((ImageView)v).setImageResource(R.drawable.setting2);
					break;
				case MotionEvent.ACTION_UP:
					((ImageView)v).setImageResource(R.drawable.setting);
					break;
				default:
					break;
				
				}
				break;
			}
			return false;
		}
		
		
	}
	private class ButtonClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.back:
					
					if(file == null || file.getAbsoluteFile().equals( Environment.getExternalStorageDirectory().getAbsoluteFile())){
						((MainActivity) context).quit();
						return;
					}
					
					file = file.getParentFile();
					//用bundler给主进程传递参数
					pathView.setText(Tools.shortPath(file.getAbsolutePath()));
					Message msg = new Message();
					msg.getData().putString("path", file.getAbsolutePath());
					handler.sendMessage(msg);
				break;
			case R.id.paste:
				optionLinearLayout.setVisibility(View.GONE);
				if("copy".equals(oprateFile_flag)){
					copyProgress = new ProgressDialog(context);
					copyProgress.setTitle(R.string.copying);
					copyProgress.setCancelable(false);
					copyProgress.show();
						new Thread(new Runnable() {	
							@Override
							public void run() {
								Tools tools = new Tools();
								try {
									if(tools.copy(operateFile, file)){
										Message msg = new Message();
										msg.getData().putBoolean("copyFlag", true);
										msg.what = 4;
										handler.sendMessage(msg);
									}else{
										Message msg = new Message();
										msg.getData().putBoolean("copyFlag", false);
										msg.what = 4;
										handler.sendMessage(msg);
									}
								} catch (Exception e) {
									Log.e("eee", e.toString());
								}
								
							}
						}).start();
					
				}else if("cut".equals(oprateFile_flag)){
					try {
						Tools tools = new Tools();
						if(tools.cutFile(operateFile, file)){
							Toast.makeText(MainActivity.this,R.string.success, 1).show();
							show(file);
						}else{
							Toast.makeText(MainActivity.this, R.string.fail, 1).show();
						}
					} catch (Exception e) {
						Log.e("eee", e.toString());
					}
				}
				
				break;
			case R.id.cancel_paste:
				optionLinearLayout.setVisibility(View.GONE);
				break;
			case R.id.setting:
				/*
				ConnectivityManager cwjManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE); 
				NetworkInfo info = cwjManager.getActiveNetworkInfo(); 
				if (info != null && info.isAvailable()){ 
					//添加积分墙
					AppConnect.getInstance(context).showOffers(context);
				} 
				else
				{
					openOptionsMenu();
				}*/
				Intent intent = new Intent(context,SettingActivity.class);
				
				startActivity(intent);
				
				break;
			case R.id.current_dir:
				Intent intent2 = new Intent(context,SettingActivity.class);
				
				startActivity(intent2);
				break;
			default:
				break;
			}
		}

	}
	/**
	 * 生成对应编号的dialog
	 * id:dialog编号	
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		return buildDialog(id);
	}
	/**
	 * 构建dialog
	 * @param id:dialog编号
	 * @return
	 */
	private Dialog buildDialog(int id){
		AlertDialog.Builder builder = new Builder(context);
		switch (id) {
		case DIALOG_CREATE:
			if("fold".equals(createFile_flag)){
				builder.setTitle(R.string.input_new_folder_name);
			}else if("file".equals(createFile_flag)){
				builder.setTitle("请输入新文件名称");
			}
			  final EditText editText = new EditText(this);
			  editText.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			  builder.setView(editText);
			  builder.setPositiveButton(R.string.verify, new DialogInterface.OnClickListener() {
				
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Tools tools = new Tools();
				boolean b = false;
				if("fold".equals(createFile_flag)){
					b = tools.createFold(operateFile, editText.getText().toString());
				}else if("file".equals(createFile_flag)){
					b = tools.createFile(operateFile, editText.getText().toString());
				}
				
				if(b){
					Toast.makeText(MainActivity.this,R.string.success, 1).show();
					show(file);
				}else{
					Toast.makeText(MainActivity.this,R.string.fail, 1).show();
				}
				
			}
			});

			  builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){

			   @Override
			   public void onClick(DialogInterface dialog, int which) {
			    dialog.dismiss();
			   
			   }
			  });
			break;
		case DIALOG_QUIT:
			  builder.setMessage(R.string.quit_or_not);
			  builder.setPositiveButton(R.string.verify, new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//结束广告
					//AppConnect.getInstance(context).finalize();
					dialog.dismiss();
				    MainActivity.this.finish();
				}
			  });
			  builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
			   @Override
			   public void onClick(DialogInterface dialog, int which) {
			    dialog.dismiss();
			   }
			  });
			break;
		case DIALOG_DELETE:
			  builder.setMessage(R.string.delete_or_not);
			  builder.setPositiveButton(R.string.verify, new DialogInterface.OnClickListener() {		
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					deleteProgress = new ProgressDialog(context);
					deleteProgress.setCancelable(false);
					deleteProgress.show();
					deleteProgress.setTitle(R.string.deleting);
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							Tools tools = new Tools();
							if(tools.delete(operateFile)){
								Message msg = new Message();
								msg.what = 3;
								msg.getData().putBoolean("delete", true);
								handler.sendMessage(msg);
								
						}else{
							Message msg = new Message();
							msg.what = 3;
							msg.getData().putBoolean("delete", true);
							handler.sendMessage(msg);
						}}
					}).start();
					
				
				}
			});
			  builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
			   @Override
			   public void onClick(DialogInterface dialog, int which) {
			    dialog.dismiss();
			   }
			  });
			  break;
		case DIALOG_RENAME:
			  builder.setTitle(R.string.input_new_file_name);
			  newFileName = new EditText(this);
			  newFileName.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			  newFileName.setText(operateFile.getName());
			  builder.setView(newFileName);
			  builder.setPositiveButton(R.string.verify, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Tools tools = new Tools();
					if(tools.renameFile(operateFile, newFileName.getText().toString())){
						Toast.makeText(MainActivity.this,R.string.success, 1).show();
						show(file);
					}else{
						Toast.makeText(MainActivity.this,R.string.fail, 1).show();
					}
					
				}
			});
			  builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
			   @Override
			   public void onClick(DialogInterface dialog, int which) {
			    dialog.dismiss();  
			   }
			  });
			  break;
		case DIALOG_PROCESS:
			  ProgressDialog dialog = new ProgressDialog(context);
			  dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			  return dialog;
			//break;
		case DIALOG_CHOUSER:
			builder.setTitle(R.string.select_file_type);
			builder.setItems(R.array.dialogchoiser, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					//intent.setData(Uri.fromFile(operateFile));
					switch(which){
					case 0:
						intent.setDataAndType(Uri.fromFile(operateFile), "text/*");
						break;
					case 1:
						intent.setDataAndType(Uri.fromFile(operateFile), "image/*");
						break;
					case 2:
						intent.setDataAndType(Uri.fromFile(operateFile), "audio/*");
						break;
					case 3:
						intent.setDataAndType(Uri.fromFile(operateFile), "video/*");
						break;
					default:
						
					}
					startActivity(Intent.createChooser(intent, getString(R.string.select_option)));
					
				}
				
				
			});
			break;
		default:
			break;
		}
		return builder.create();
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
	
	private void quit(){
		if(System.currentTimeMillis()-exitTime>2000){
			Toast.makeText(context, "再按一次退出程序", Toast.LENGTH_SHORT).show();
			exitTime = System.currentTimeMillis();
		}else{
			 finish();
	         System.exit(0);
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
