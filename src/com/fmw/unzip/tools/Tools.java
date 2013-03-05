package com.fmw.unzip.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.box.unzip.R;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class Tools {
	private static Context context;
	
		public Tools() {};
		public Tools(Context context) {
		super();
		Tools.context = context;
	}
		

		//获取文件列表
		public static List<HashMap<String, Object>> getFileList(File dir,boolean showHide,Context context){
			List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String,Object>>();
			File[] files = dir.listFiles();
			if(files==null||files.length==0){
				return null;
			}
			String data[]=dir.list();
			for(File file : files){
				if(file.isHidden()&&!showHide){
					continue;
				}
				HashMap<String, Object> item = new HashMap<String, Object>();
				String name = file.getName();
				String filesize ="0";
				String fileType = "0";
				if(file.isFile()){
					Long fileLen = file.length();
					//获取文件大小
					if(fileLen<1024){
						filesize = fileLen+"B";
					}else if(fileLen<1024*1024){
						filesize = fileLen/1024+"KB";
					}
					else if(fileLen<1024*1024*1024){
						filesize = fileLen/(1024*1024)+"MB";
					}else{
						filesize = fileLen/(1024*1024*1024)+"GB";
					}
					fileType=getMimeType(file.getAbsolutePath());
					
					item.put("item_len", fileLen);
					item.put("item_type", fileType);
					if("text/plain".equals(fileType)){
						item.put("item_img", R.drawable.txt);
					}else if("text/html".equals(fileType)){
						item.put("item_img", R.drawable.html);
					}else if("text/htm".equals(fileType)){
						item.put("item_img", R.drawable.htm);
					}else if("image/x-ms-bmp".equals(fileType)){
						item.put("item_img", R.drawable.bmp);
					}else if("application/msword".equals(fileType)){
						item.put("item_img", R.drawable.doc);
					}else if("image/gif".equals(fileType)){
						item.put("item_img", R.drawable.gif);
					}else if("image/jpeg".equals(fileType)||"image/jpg".equals(fileType)){
						item.put("item_img", R.drawable.jpg);
					}else if("application/pdf".equals(fileType)){
						item.put("item_img", R.drawable.pdf);
					}else if("image/png".equals(fileType)){
						item.put("item_img", R.drawable.png);
					}else if("application/vnd.ms-powerpoint".equals(fileType)){
						item.put("item_img", R.drawable.ppt);
					}else if("application/vnd.ms-excel".equals(fileType)){
						item.put("item_img", R.drawable.xls);
					}else if("application/x-gzip".equals(fileType)){
						item.put("item_img", R.drawable.zip);
					}else if("application/octet-stream".equals(fileType)){
						item.put("item_img", R.drawable.rar);
					}else if("text/xml".equals(fileType)){
						item.put("item_img", R.drawable.xml);
					}else if("application/pdf".equals(fileType)){
						item.put("item_img", R.drawable.pdf);
					}else if("application/mshelp".equals(fileType)){
						item.put("item_img", R.drawable.chm);
					}else if("video/x-msvideo".equals(fileType)){
						item.put("item_img", R.drawable.avi);
					}else if("video/mp4".equals(fileType)){
						
						item.put("item_img", context.getResources().getDrawable(R.drawable.mp4));
					}else if("audio/x-mpeg".equals(fileType)){
						item.put("item_img", R.drawable.mp3);
					}else if("audio/x-wav".equals(fileType)){
						item.put("item_img", R.drawable.wav);
					}else if("application/vnd.android.package-archive".equals(fileType)){
						Drawable drawable = loadUninstallApkIcon(context,file.getAbsolutePath());
						if(drawable !=null){
							item.put("item_img",drawable);
						}else{
							item.put("item_img",R.drawable.apk);
						}
						
					}else{
						item.put("item_img", R.drawable.no);
					}

				}else if(file.isDirectory()){
					if(!file.isHidden()){
						filesize="("+file.list().length+")";
					}else{
						filesize="(Hide)";
					}
					item.put("item_len", -1);
					fileType = "000/DIR";
					item.put("item_img", R.drawable.dir);
					
				}
				//获取文件最后修改时间
				Date lastModify = new Date(file.lastModified());
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String item_time = dateFormat.format(lastModify);
				
				item.put("item_icon", fileType);
				item.put("item_size", filesize);
				item.put("item_name", name);
				item.put("item_time", item_time);
				item.put("item_path", file.getAbsolutePath());
				item.put("item_type", fileType);
				dataList.add(item);
			}
			return dataList;
		}
		
		//List<HashMap<String, String>> 排序
		public static List<HashMap<String, Object>> sortList(List<HashMap<String, Object>> dataList,String type){
			if("name".equals(type)){
				Collections.sort(dataList, new Comparator<HashMap<String, Object>>() {

					@Override
					public int compare(HashMap<String, Object> o1,HashMap<String, Object> o2) {
						int flag = o1.get("item_name").toString().toLowerCase().compareTo(o2.get("item_name").toString().toLowerCase());
						return flag;
					}
				});
			}else if("time".equals(type)){
				Collections.sort(dataList, new Comparator<HashMap<String, Object>>() {

					@Override
					public int compare(HashMap<String, Object> o1,HashMap<String, Object> o2) {
						int flag = o1.get("item_time").toString().compareTo(o2.get("item_time").toString());
						return flag;
					}
				});
			}else if("size".equals(type)){
				Collections.sort(dataList, new Comparator<HashMap<String, Object>>() {

					@Override
					public int compare(HashMap<String, Object> o1,HashMap<String, Object> o2) {
						int flag = Integer.parseInt(o1.get("item_len").toString())-Integer.parseInt(o2.get("item_len").toString());
						if(flag==0){
							flag = o1.get("item_name").toString().toLowerCase().compareTo(o2.get("item_name").toString().toLowerCase());
						}
						return flag;
					}
				});
			}else if("type".equals(type)){
				Collections.sort(dataList, new Comparator<HashMap<String, Object>>() {

					@Override
					public int compare(HashMap<String, Object> o1,HashMap<String, Object> o2) {
						int flag = o1.get("item_type").toString().compareTo(o2.get("item_type").toString());
						if(flag==0){
							flag = o1.get("item_name").toString().toLowerCase().compareTo(o2.get("item_name").toString().toLowerCase());
						}
						return flag;
					}
				});
			}
			
			return dataList;
		}

		//获取文件类型
		public static String getMimeType(String fileUrl){   
			      /*FileNameMap fileNameMap = URLConnection.getFileNameMap();   
			      String type = fileNameMap.getContentTypeFor(fileUrl);
			      if(type!=null){
			    	  return type;  
			      }*/
				String fileNameExt = fileUrl.substring(fileUrl.lastIndexOf(".")+1).toLowerCase();
				if(new File(fileUrl).isDirectory()){
					return "000DIR";
				}
				if("txt".equals(fileNameExt)){
					return "text/plain";
				}else if("doc".equals(fileNameExt)){
					return "application/msword";
				}else if("html".equals(fileNameExt)){
					return "text/html";
				}else if("htm".equals(fileNameExt)){
					return "text/htm";
				}else if("bmp".equals(fileNameExt)){
					return "image/x-ms-bmp";
				}else if("gif".equals(fileNameExt)){
					return "image/gif";
				}else if("jpeg".equals(fileNameExt)||"jpg".equals(fileNameExt)){
					return "image/jpeg";
				}else if("pdf".equals(fileNameExt)){
					return "application/pdf";
				}else if("png".equals(fileNameExt)){
					return "image/png";
				}else if("ppt".equals(fileNameExt)||"pps".equals(fileNameExt)){
					return "application/vnd.ms-powerpoint";
				}else if("xls".equals(fileNameExt)){
					return "application/vnd.ms-excel";
				}else if("zip".equals(fileNameExt)){
					return "application/x-gzip";
				}else if("rar".equals(fileNameExt)){
					return "application/octet-stream";
				}else if("xml".equals(fileNameExt)){
					return "text/xml";
				}else if("pdf".equals(fileNameExt)){
					return "application/pdf";
				}else if("chm".equals(fileNameExt)){
					return "application/mshelp";
				}else if("avi".equals(fileNameExt)){
					return "video/x-msvideo";
				}else if("mp4".equals(fileNameExt)){
					return "video/mp4";
				}else if("mp3".equals(fileNameExt)){
					return "audio/x-mpeg";
				}else if("wav".equals(fileNameExt)){
					return "audio/x-wav";
				}else if("apk".equals(fileNameExt)){
					return "application/vnd.android.package-archive";
				}else{
					return "zzz";
				}
			      
		}  
		
		//新建子文件夹
		public boolean createFold(File parentFile,String name){
			File newFile = new File(parentFile, name);
			if(!newFile.exists()){
				if(newFile.mkdir()){
					return true;
				}
			}
			return false;
		}
		
		//新建子文件
		public boolean createFile(File parentFile,String name){
			File newFile = new File(parentFile, name);
			if(!newFile.exists()){
				try {
					if(newFile.createNewFile()){
						return true;
					}
				} catch (IOException e) {
					Log.e("eee", e.toString());
				}
			}
			return false;
		}
		
		//删除文件或文件夹
		public boolean delete(File file){
			if(file.isDirectory()&&file.list()!=null){
				for(File subFile : file.listFiles()){
					if(!delete(subFile)){
						return false;
					}
				}
			}
			
			return file.delete();
		}
		
		//文件重命名
		public boolean renameFile(File file,String name){
			return file.renameTo(new File(file.getParentFile(),name));
			
		}
		
		//文件复制
		public boolean copyFile(File sourceFile,File targetFile) throws Exception{
			if(sourceFile.isFile()){
				FileInputStream inputStream=null;
				FileOutputStream outputStream=null;
				try {
					inputStream = new FileInputStream(sourceFile);
					byte [] buffer = new byte[1024];
					int len=0;
					outputStream = new FileOutputStream(new File(targetFile,sourceFile.getName()));
					while((len=inputStream.read(buffer))!=-1){
						outputStream.write(buffer,0,len);
					}
					return true;
				} catch (Exception e) {
					throw e;
				}finally{
					if(outputStream!=null) outputStream.close();
					if(inputStream!=null) inputStream.close();
				}
			
			}
			return false;
		}
		
		//文件夹复制,复制文件夹内所有内容
		public boolean copyDir(File sourceFile,File targetFile)throws Exception{
			boolean b= true;
			if(sourceFile.isDirectory()){
				new File(targetFile,sourceFile.getName()).mkdirs();
				for(File subFile : sourceFile.listFiles()){
					if(subFile.isDirectory()){
						b=copyDir(subFile, new File(targetFile,sourceFile.getName()));
						if(!b){
							return b;
						}
						
					}else{
						b=copyFile(subFile, new File(targetFile,sourceFile.getName()));
						if(!b){
							return b;
						}
					}
				}
				
			}
			return b;
		}
		
		//复制操作
		public boolean copy(File sourceFile,File targetFile)throws Exception{
			if(sourceFile.isDirectory()){
				return copyDir(sourceFile, targetFile);
			}else if(sourceFile.isFile()){
				return copyFile(sourceFile, targetFile);
			}
			return false;
		}
		
		//剪切文件
		public boolean cutFile (File sourceFile,File targetDir){
			//文件、文件夹都可剪切
			if(targetDir.isDirectory()){
				return sourceFile.renameTo(new File(targetDir,sourceFile.getName()));
			}
			
			return false;
		}
		
		//缩短路径
		public static String shortPath(String path){
			return path.substring(5);
			
		}
		
		public static boolean existZH(String str) {
			String regEx = "[\\u4e00-\\u9fa5]";
			Pattern p = Pattern.compile(regEx);
			Matcher m = p.matcher(str);
			while (m.find()) {
				return true;
			}
			return false;
		}
		
		public static Drawable loadUninstallApkIcon(Context context, String archiveFilePath) {  
	        PackageManager pm = context.getPackageManager();  
	        PackageInfo info = null;
	        if(pm!=null){
	        	info = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES); 
	        }else{
	        	return null;
	        }
	        if(info == null){
	        	return null;
	        } 
	        ApplicationInfo appInfo = info.applicationInfo;  
	        if(appInfo == null){
	        	return null;
	        } 
	        appInfo.sourceDir = archiveFilePath;  
	        appInfo.publicSourceDir = archiveFilePath;  
	  
	        return appInfo.loadIcon(pm);  
	  
	    } 
}
