package com.greenwich.flowerplus.infrastructure.storage.cloudinary;

import com.cloudinary.Search;
import com.cloudinary.api.ApiResponse;

import java.util.List;
import java.util.Map;

public interface CloudinaryClient {
    Map<String, Object> upload(byte[] bytes, Map<String, Object> options);
    void delete(String publicId);

    void deleteResources(List<String> publicIds);
    ApiResponse searchByTag(String tag) throws Exception;

    void removeTag(String tag, List<String> publicIds);
}
