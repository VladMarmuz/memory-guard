package com.catchthemoment.controller;

import com.catchthemoment.entity.Image;
import com.catchthemoment.exception.ServiceProcessingException;
import com.catchthemoment.mappers.ImageMapper;
import com.catchthemoment.model.ImageModel;
import com.catchthemoment.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static com.catchthemoment.exception.ApplicationErrorEnum.EMPTY_REQUEST;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageController implements ImageControllerApiDelegate {

    private final ImageService imageService;
    private final ImageMapper imageMapper;

    @Override
    public ResponseEntity<ImageModel> uploadImage(MultipartFile file) throws Exception {
        log.info("*** Received an upload image request with file name: {} ***", file.getOriginalFilename());
        if (!file.isEmpty()) {
            Image uploadedImage = imageService.uploadImage(file);
            log.info("*** Upload was successful ***");
            ImageModel currentImage = imageMapper.toModel(uploadedImage);
            return ResponseEntity.ok(currentImage);
        } else {
            throw new ServiceProcessingException(
                    EMPTY_REQUEST.getCode(),
                    EMPTY_REQUEST.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> downloadImage(String name) throws Exception {
        log.info("*** Received a download image request by name: {} ***", name);
        Resource resource = imageService.downloadImage(name);
        log.info("*** Download was successful ***");
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resource);
    }

    @Override
    public ResponseEntity<List<ImageModel>> uploadImages(List<MultipartFile> images) throws Exception {
        List<ImageModel> savingImages = new ArrayList<>();
        for (MultipartFile image : images) {
            Image currentImage = imageService.uploadImage(image);
            ImageModel currentModel = imageMapper.toModel(currentImage);
            savingImages.add(currentModel);
        }
        return ResponseEntity.ok().body(savingImages);
    }


    @Override
    public ResponseEntity<Object> deleteImage(String name) throws Exception {
        log.info("*** Received a delete image request by name: {} ***", name);
        imageService.deleteImage(name);
        log.info("*** Image successfully deleted ***");
        return ResponseEntity.ok("Image successfully deleted");
    }

    @Override
    public ResponseEntity<ImageModel> addDescription(ImageModel imageModel) throws Exception {
        Image image = imageService.addDescription(imageModel);
        ImageModel currentModel = imageMapper.toModel(image);
        return ResponseEntity.ok(currentModel);
    }

    @Override
    public ResponseEntity<Object> deleteImages(List<String> requestBody) throws Exception {
        log.info("*** Received a delete images request by names: {} ***", requestBody);
        for (String name : requestBody){
            imageService.deleteImage(name);
        }
        log.info("*** Images successfully deleted ***");
        return ResponseEntity.ok("Images successfully deleted");
    }
}