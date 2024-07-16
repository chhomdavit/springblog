package com.davit.springblog.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {

	byte[] getFileName(String fileName);

    String saveFile(MultipartFile file, String path) throws IOException;
 
    String saveBase64Image(String path, byte[] base64Image) throws IOException;
}
