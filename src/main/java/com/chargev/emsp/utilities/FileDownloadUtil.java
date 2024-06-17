package com.chargev.emsp.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
 
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
 
public class FileDownloadUtil {
    public Resource getFileAsResource(String filePath) throws IOException {
        Path filePathUri =  Paths.get(filePath);

        if (!Files.exists(filePathUri)) {
            return null;
        }

        return new UrlResource(filePathUri.toUri());
    }

    public Resource getFileAsResource() throws IOException {
        Path dirPath = Paths.get("C:/iWaterAI/upload/falsevevnt/response.zip");

        if (!Files.exists(dirPath)) {
            return null;
        }

        return new UrlResource(dirPath.toUri());
    }
}