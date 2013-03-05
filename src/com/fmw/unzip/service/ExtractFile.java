package com.fmw.unzip.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;

import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import com.fmw.unzip.ibz.IExtractFile;
import com.fmw.unzip.tools.Tools;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.rarfile.FileHeader;

import android.util.Log;

public class ExtractFile {
	private File sourceFile;
	private File desDir;
	
	public ExtractFile(File sourceFile, File desDir) {
		super();
		this.sourceFile = sourceFile;
		this.desDir = desDir;
	}

	public void unZip(IExtractFile iExtractFile){
		
		 try {
			if(desDir == null){
				desDir = new File(this.sourceFile.getParentFile(),this.sourceFile.getName().substring(0,this.sourceFile.getName().lastIndexOf(".") )); 
			}
			if (!desDir.exists()) {
			    desDir.mkdirs();
			}
			ZipFile zf = new ZipFile(this.sourceFile, "GBK");
			Enumeration em = zf.getEntries();
			int i = 0;
			while (em.hasMoreElements()){
				i++;
				em.nextElement();
			}
			
			//回调函数
			iExtractFile.setMax(i);
			
			em = zf.getEntries();
			i = 0;
			while (em.hasMoreElements()){
				ZipEntry entry = (ZipEntry)em.nextElement();
				File file = new File(desDir,entry.getName());
				Log.i("ppp1", file.getAbsolutePath());
				if(file.isDirectory()||entry.getName().substring(entry.getName().length()-1).equals("/")){
					file.mkdirs();
					i++;
					iExtractFile.onExtract(i);
				}else{
					File parentFile = file.getParentFile();
					
					if(!parentFile.exists()||parentFile.isFile()){
						parentFile.mkdirs();
					}
					InputStream in = zf.getInputStream(entry);
					
					FileOutputStream out = new FileOutputStream(file);
					byte[] buffer =new byte[1024*5];
					int len =0 ;
					while ((len = in.read(buffer))!=-1){
						out.write(buffer, 0, len);
					}
					out.close();
					in.close();
					i++;
					iExtractFile.onExtract(i);
				}
				
			}
		
		} catch (Exception e) {
			int line = e.getStackTrace()[0].getLineNumber();
			String name = e.getStackTrace()[0].getFileName();
			Log.e("eee", "line:"+line+"  name:"+name);
			iExtractFile.onExtract(-1);
		}
	}
	
	public void unRAR(IExtractFile iExtractFile){
		

		if(desDir == null){
			desDir = new File(this.sourceFile.getParentFile(),sourceFile.getName().substring(0, sourceFile.getName().lastIndexOf(".")));
		}
		
	    if (!desDir.exists()) { //目标目录不存在时，创建该文件夹
	        desDir.mkdirs();
	    }
	    try {
	    	
	    	Archive archive = new Archive(sourceFile);
	    	List<FileHeader> headers = archive.getFileHeaders();
	    	iExtractFile.setMax(headers.size());
	    	int i=0;
	    	for(FileHeader header:headers){
	    		
	    		String name=  header.getFileNameW().trim();
				if(!Tools.existZH(name)){
					name = header.getFileNameString().trim();
				}
	    		name = name.replace("\\", "/");
	    		File fileName = new File(desDir,name);
	    		if(fileName.isDirectory()){
	    			fileName.mkdirs();
	    		}else {
	    			if(!fileName.getParentFile().exists()){
	    				fileName.getParentFile().mkdirs();
	    			}
	    			FileOutputStream out = new FileOutputStream(fileName);
	    			//BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(out,1024*5);
	    			archive.extractFile(header, out);
	    			out.close();
	    		}
	    		i++;
	    		//回调函数
	    		iExtractFile.onExtract(i);
	    	}
	    	
	       } catch (Exception e) {
				
				int line = e.getStackTrace()[0].getLineNumber();
				String name = e.getStackTrace()[0].getFileName();
				Log.e("eee", "line:"+line+"  name:"+name);
				//回调函数
	    		iExtractFile.onExtract(-1);
	       }
	}
	
	public void unTar(IExtractFile iExtractFile) throws Exception{
		if(desDir == null){
			desDir = new File(this.sourceFile.getParentFile(),sourceFile.getName().substring(0, sourceFile.getName().lastIndexOf(".")));
		}
		InputStream inputStream = null;
		TarInputStream tarInputStream = null;
		FileOutputStream fileOutputStream = null;
		try {
			inputStream = new FileInputStream(sourceFile);
			tarInputStream = new TarInputStream(inputStream);
			TarEntry entry = null;
			int i = 0;
			while (tarInputStream.getNextEntry()!=null){
				i++;
			}
			
			//回调函数
			iExtractFile.setMax(i);
			i = 0;
			tarInputStream = null;
			tarInputStream = new TarInputStream(new FileInputStream(sourceFile));
			while((entry = tarInputStream.getNextEntry())!=null){
				File file = new File(desDir,entry.getName());
				if(file.isDirectory()||entry.getName().substring(entry.getName().length()-1).equals("/")){
					file.mkdirs();
				}else{
					File parentFile = file.getParentFile();
					if(!parentFile.exists()){
						parentFile.mkdirs();
					}
					fileOutputStream = new FileOutputStream(file);
					byte []buffer = new byte[1024];
					int len = 0;
					while((len=tarInputStream.read(buffer))!=-1){
						fileOutputStream.write(buffer, 0, len);
					}
					
				}
				
				i++;
	    		//回调函数
	    		iExtractFile.onExtract(i);
				
			}
		} catch (Exception e) {
			int line = e.getStackTrace()[0].getLineNumber();
			String name = e.getStackTrace()[0].getFileName();
			//回调函数
    		iExtractFile.onExtract(-1);
			throw e;
		}finally{
			if(fileOutputStream!=null)fileOutputStream.close();
			if(tarInputStream!=null)tarInputStream.close();
			if(inputStream!=null)inputStream.close();
		}
		
	}
	
}


















