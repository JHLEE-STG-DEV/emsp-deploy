package com.chargev.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalFileManager {
    // 디렉토리 보장
    public static void ensureDirectory(Path directoryPath) throws IOException{
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }
    }

    // 파일 작성 성공 시 true 반환
    public static boolean writeToFile(String content, Path filePath) throws IOException {
        try {
            Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
            return true; 
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static String readFromFile(Path filePath) throws IOException {
        String content = "";
        try {
            content = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        return content;
    }
}
