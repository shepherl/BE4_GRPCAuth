package com.school21.shopapi.controller;

import com.school21.shopapi.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Tag(name = "Image API", description = "Operations related to product images")
public class ImageController {

    private final ImageService imageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Add image to a product")
    public ResponseEntity<UUID> addImage(
            @RequestParam UUID productId,
            @RequestPart MultipartFile file) throws IOException {
        return new ResponseEntity<>(imageService.addImage(productId, file), HttpStatus.CREATED);
    }

    @PatchMapping(value = "/{imageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Change image")
    public ResponseEntity<Void> changeImage(
            @PathVariable UUID imageId,
            @RequestPart MultipartFile file) throws IOException {
        imageService.changeImage(imageId, file);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{imageId}")
    @Operation(summary = "Delete image by ID")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID imageId) {
        imageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{imageId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(summary = "Get image data by image ID")
    public ResponseEntity<byte[]> getImage(@PathVariable UUID imageId) {
        byte[] data = imageService.getImageData(imageId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "image_" + imageId + ".bin");
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/product/{productId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(summary = "Get image data by product ID")
    public ResponseEntity<byte[]> getImageByProductId(@PathVariable UUID productId) {
        byte[] data = imageService.getImageDataByProductId(productId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "product_image_" + productId + ".bin");
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
