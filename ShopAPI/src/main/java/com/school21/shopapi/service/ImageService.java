package com.school21.shopapi.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.UUID;

public interface ImageService {
    UUID addImage(UUID productId, MultipartFile file) throws IOException;
    void changeImage(UUID imageId, MultipartFile file) throws IOException;
    void deleteImage(UUID imageId);
    byte[] getImageData(UUID imageId);
    byte[] getImageDataByProductId(UUID productId);
}
