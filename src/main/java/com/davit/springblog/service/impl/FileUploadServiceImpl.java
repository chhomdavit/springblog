package com.davit.springblog.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.davit.springblog.service.FileUploadService;

@Service
public class FileUploadServiceImpl implements FileUploadService{

	@Override
	public byte[] getFileName(String fileName) {
	    try {
            Path filename = Paths.get("upload", fileName);
            return Files.readAllBytes(filename);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
	}

	@Override
	public String saveFile(MultipartFile file, String path) throws IOException {
	       if (file == null || file.isEmpty()) {
	            throw new IllegalArgumentException("File is empty");
	        }

	        File directory = new File(path);
	        if (!directory.exists()) {
	            directory.mkdirs();
	        }

	        String originalFileName = file.getOriginalFilename();
	        if (originalFileName == null) {
	            throw new IllegalArgumentException("Original file name is null");
	        }
	        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
	        String newFileName = UUID.randomUUID().toString() + fileExtension;

	        Path filePath = Paths.get(path, newFileName);
	        Files.copy(file.getInputStream(), filePath);

	        return newFileName;
	}

	@Override
	public String saveBase64Image(String path, byte[] base64Image) throws IOException {
		 	String uniqueId = UUID.randomUUID().toString();
	        String newFileName = uniqueId + ".png"; 
	        String fullPath = path + File.separator + newFileName;

	        Files.createDirectories(Paths.get(path)); 
	        Files.write(Paths.get(fullPath), base64Image);

	        return newFileName;
	}

}
