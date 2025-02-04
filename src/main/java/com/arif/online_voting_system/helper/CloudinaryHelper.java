package com.arif.online_voting_system.helper;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CloudinaryHelper {
    
    @Value("${cloudinary.cloud_name}")
    private String cloudName;
    
    @Value("${cloudinary.api_key}")
    private String apiKey;
    
    @Value("${cloudinary.api_secret}")
    private String apiSecret;
    
    public String saveImage(MultipartFile profilePic) {
        Map config = ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret
        );
        
        Cloudinary cloudinary = new Cloudinary(config);
        Map uploadParams = ObjectUtils.asMap("folder", "profile_pictures");
        
        try {
            Map uploadResult = cloudinary.uploader().upload(profilePic.getBytes(), uploadParams);
            return (String) uploadResult.get("url");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

