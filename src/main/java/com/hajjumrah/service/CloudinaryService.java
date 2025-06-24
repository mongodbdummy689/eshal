package com.hajjumrah.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.hajjumrah.model.Image;
import com.hajjumrah.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private ImageRepository imageRepository;

    public String uploadImage(MultipartFile file, String productId, String uploadedBy) throws IOException {
        String publicId = file.getOriginalFilename().replaceAll("[^a-zA-Z0-9]", "_");
        
        Map<String, Object> uploadResult = cloudinary.uploader().upload(
            file.getBytes(),
            ObjectUtils.asMap(
                "folder", "hajj-umrah-products",
                "public_id", publicId,
                "overwrite", true
            )
        );
        
        String secureUrl = (String) uploadResult.get("secure_url");
        String cloudinaryPublicId = (String) uploadResult.get("public_id");
        
        // Save image metadata to MongoDB
        Image image = new Image();
        image.setTitle(file.getOriginalFilename());
        image.setDescription("Product image for " + productId);
        image.setUrl(secureUrl);
        image.setCloudinaryPublicId(cloudinaryPublicId);
        image.setProductId(productId);
        image.setUploadedAt(LocalDateTime.now());
        image.setUploadedBy(uploadedBy);
        
        imageRepository.save(image);
        
        return secureUrl;
    }

    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        
        // Also delete from MongoDB
        imageRepository.findByCloudinaryPublicId(publicId)
            .ifPresent(imageRepository::delete);
    }
} 