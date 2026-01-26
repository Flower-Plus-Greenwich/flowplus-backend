package com.greenwich.flowerplus.infrastructure.storage;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

public interface FileStorageService {

    /**
     *  Upload file and return the url
     * */
    default String uploadFile(MultipartFile file) {
        return file.getOriginalFilename();
    }

    /**
     *  Delete file by URL
     * */
    default void deleteFile(String fileUrl) {}

    /**
     *  Upload multiple files and return the list urls
     * */
    default List<String> uploadMultipleFiles(List<MultipartFile> files) {
        return List.of();
    }

    /**
     * 👇 MỚI: Hàm xác nhận file chính thức (gỡ tag "temporary")
     * ProductService sẽ gọi hàm này sau khi save DB thành công.
     */
    default void confirmFiles(List<String> fileUrls) {}

    default Path saveImportFile(Long jobId, MultipartFile file) {
        return null;
    }
}
