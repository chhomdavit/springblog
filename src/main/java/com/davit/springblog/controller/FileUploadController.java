package com.davit.springblog.controller;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.davit.springblog.service.FileUploadService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class FileUploadController {

	private final FileUploadService fileUploadService;
	
	@Value("${project.upload}")
    private String path;
	
	@GetMapping("/auth/{fileName}")
    public ResponseEntity<ByteArrayResource> getFileName(@PathVariable String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            byte[] buffer = fileUploadService.getFileName(fileName);
            if (buffer != null) {
                ByteArrayResource byteArrayResource = new ByteArrayResource(buffer);
                final MediaType image_PNG2 = MediaType.IMAGE_PNG;
                if (image_PNG2 != null) {
                    return ResponseEntity.ok()
                            .contentLength(buffer.length)
                            .contentType(image_PNG2)
                            .body(byteArrayResource);
                } else {

                    return null;
                }
            }
        }
        return ResponseEntity.badRequest().build();
    }
	
	
	@PostMapping("/auth/upload")
    public ResponseEntity<String> uploadFileHandler(@RequestParam MultipartFile file) throws IOException {
        String uploadedFileName = fileUploadService.saveFile(file, path);
        return ResponseEntity.ok("http://localhost:1001/auth/" + uploadedFileName);
    }
	

    @PostMapping("/auth/upload/base64")
    public ResponseEntity<String> uploadBase64ImageHandler(@RequestBody String base64Image) throws IOException {

        if (base64Image == null || base64Image.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid base64 image data");
        }

        String base64ImageData = base64Image;
        if (base64Image.contains(",")) {
            base64ImageData = base64Image.split(",")[1];
        }

        byte[] decodedImage = Base64.getMimeDecoder().decode(base64ImageData);
        String uploadedFileName = fileUploadService.saveBase64Image(path, decodedImage);
        return ResponseEntity.ok("Base64 image upload: " + uploadedFileName);
    }

}
