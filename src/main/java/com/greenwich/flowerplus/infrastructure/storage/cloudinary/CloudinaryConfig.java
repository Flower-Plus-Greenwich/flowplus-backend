package com.greenwich.flowerplus.infrastructure.storage.cloudinary;

import com.cloudinary.Cloudinary;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Component
public class CloudinaryConfig {

    private final Cloudinary cloudinary;

    public CloudinaryConfig(CloudinaryProperties properties) {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", properties.getCloudName());
        config.put("api_key", properties.getApiKey());
        config.put("api_secret", properties.getApiSecret());

        this.cloudinary = new Cloudinary(config);
    }

}
