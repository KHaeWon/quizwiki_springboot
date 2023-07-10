package com.multi.quizwiki.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadLogicService {
	@Value("${file.dir}")
	private String uploadpath;
	
	public String getUploadpath(String filename) {
		return uploadpath + filename;
	}
	public String getUploadpath(String path,String filename) {
		return uploadpath + path + File.separator + filename;
	}
	
	//파일 한 개를 업로드하고 저장된 파일명을 리턴하는 메소드
	public String uploadFile(MultipartFile multipartFile, String path) throws IllegalStateException, IOException {
		String storeFilename ="";
		if(!multipartFile.isEmpty()) {
			String originalFilename = multipartFile.getOriginalFilename();
			storeFilename = createStoreFilename(originalFilename);
			multipartFile.transferTo(new File(uploadpath+File.separator+path+File.separator+storeFilename));
		}
		return storeFilename;
	}
	
	public String uploadFile(String base64, String originName, String path) throws IOException {
		String storeFilename = createStoreFilename(originName);
		String slice = base64.substring(base64.indexOf(",")+1);
		byte[] bytes = DatatypeConverter.parseBase64Binary(slice);
		
		FileUtils.writeByteArrayToFile(new File(uploadpath+File.separator+path+File.separator+storeFilename), bytes);
		
		return storeFilename;
	}
	
	public String uploadFile(MultipartFile multipartFile) throws IllegalStateException, IOException {
		String storeFilename ="";
		if(!multipartFile.isEmpty()) {
			String originalFilename = multipartFile.getOriginalFilename();
			storeFilename = createStoreFilename(originalFilename);
			multipartFile.transferTo(new File(getUploadpath(storeFilename))); 
		}
		return storeFilename;
	}
	
	//UUID를 이용해서 파일명을 변경해서 리턴하는 메소드
	private String createStoreFilename(String originalFilename) {
		int pos = originalFilename.lastIndexOf(".");
		String ext = originalFilename.substring(pos+1);//시작index만 지정하면 시작index부터 끝까지 문자열 추출
		String uuid = UUID.randomUUID().toString();
		return uuid+"."+ext;
	}
}
