package com.chargev.emsp.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;
 
public class FileUploadUtil {
    public static String saveFile(String fileName, MultipartFile multipartFile)
            throws IOException {
        Path uploadPath = Paths.get("../Movies");
          
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
 
        String randomFileName = UUID.randomUUID().toString().replace("-", "");
        String hashValue = null;
         
        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(randomFileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            hashValue = HashingUtil.sha256Hash(filePath.toString());
        } catch (IOException ioe) {       
            throw new IOException("Could not save file: " + fileName, ioe);
        }
         
        return hashValue + randomFileName;
    }
}
