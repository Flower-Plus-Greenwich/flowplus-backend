package com.greenwich.flowerplus.infrastructure.storage.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;

import com.greenwich.flowerplus.common.enums.ErrorCode;
import com.greenwich.flowerplus.common.exception.AppException;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.Map;

@Component
public class CloudinaryClientImpl implements CloudinaryClient {

    private final Cloudinary cloudinary;

    public CloudinaryClientImpl(CloudinaryConfig cloudinaryConfig) {
        this.cloudinary = cloudinaryConfig.getCloudinary();
    }

    @Override
    public Map<String, Object> upload(byte[] bytes, Map<String, Object> options) {
        try {
            return cloudinary.uploader().upload(bytes, options);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cloudinary upload failed: " + e);
        }
    }

    @Override
    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cloudinary delete failed: " + e);
        }
    }

    @Override
    public void deleteResources(List<String> publicIds) {
        try {
            if (publicIds == null || publicIds.isEmpty()) return;
            // api() là Admin API, khác với uploader()
            cloudinary.api().deleteResources(publicIds, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cloudinary bulk delete failed", e);
        }
    }

    @Override
    public ApiResponse searchByTag(String tag) throws Exception {
        // Tìm ảnh có tag cụ thể VÀ tạo hơn 1 ngày trước (created_at < 1d)
        // expression syntax: https://cloudinary.com/documentation/search_api#expressions
        return cloudinary.search()
                .expression("tags:" + tag + " AND created_at<1d")
                .maxResults(500) // Giới hạn 1 lần xóa 500 cái cho an toàn
                .execute();
    }

    @Override
    public void removeTag(String tag, List<String> publicIds) {
        try {
            if (publicIds == null || publicIds.isEmpty()) return;
            // Gọi uploader để gỡ tag
            cloudinary.uploader().removeTag(tag, publicIds.toArray(new String[0]), ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Remove tag failed ", e);
        }
    }
}
