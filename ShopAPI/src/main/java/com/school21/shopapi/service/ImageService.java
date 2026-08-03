package com.school21.shopapi.service;

import com.school21.shopapi.entity.Image;
import com.school21.shopapi.entity.Product;
import com.school21.shopapi.exception.ResourceNotFoundException;
import com.school21.shopapi.repository.ImageRepository;
import com.school21.shopapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final ProductRepository productRepository;

    @Transactional
    public UUID addImage(UUID productId, MultipartFile file) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Image image = new Image();
        image.setData(file.getBytes());
        Image savedImage = imageRepository.save(image);

        product.setImage(savedImage);
        productRepository.save(product);

        return savedImage.getId();
    }

    @Transactional
    public void changeImage(UUID imageId, MultipartFile file) throws IOException {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));

        image.setData(file.getBytes());
        imageRepository.save(image);
    }

    @Transactional
    public void deleteImage(UUID imageId) {
        if (!imageRepository.existsById(imageId)) {
            throw new ResourceNotFoundException("Image not found with id: " + imageId);
        }
        imageRepository.deleteById(imageId);
    }

    public byte[] getImageData(UUID imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));
        return image.getData();
    }

    public byte[] getImageDataByProductId(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        if (product.getImage() == null) {
            throw new ResourceNotFoundException("No image associated with product id: " + productId);
        }

        return product.getImage().getData();
    }
}
