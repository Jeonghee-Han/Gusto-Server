package com.umc.gusto.global.util;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


     // 파일 업로드 시 파일명을 난수화(중복 방지)하기 위해 random 으로 돌림
    private String createFileName(String fileName) {
        return UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }

    /**
     * file 형식이 잘못된 경우를 확인하기 위해 만들어진 로직
     * 파일 타입과 상관없이 업로드할 수 있게 하기 위해 .의 존재 유무만 판단
     */
    private String getFileExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "잘못된 형식의 파일(" + fileName + ") 입니다.");
        }
    }

    // MultipartFile 단일 이미지를 S3에 업로드
    public String uploadImage(MultipartFile image) {
        String fileName = createFileName(image.getOriginalFilename());

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(image.getSize());
        objectMetadata.setContentType(image.getContentType());

        try (InputStream inputStream = image.getInputStream()) {
            amazonS3.putObject(
                    new PutObjectRequest(bucket, fileName, inputStream, objectMetadata));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "파일 업로드에 실패했습니다.");
        }
        log.info("편리한 이미지 정보 확인 로그 :"+fileName);
        return amazonS3.getUrl(bucket, fileName).toString();
    }


     // MultipartFile List 다수의 이미지를 S3에 업로드
     public List<String> uploadImages(List<MultipartFile> images) {
         List<String> fileUrlList = new ArrayList<>();

         images.forEach(file -> {
             String fileName = createFileName(file.getOriginalFilename());

             ObjectMetadata objectMetadata = new ObjectMetadata();
             objectMetadata.setContentLength(file.getSize());
             objectMetadata.setContentType(file.getContentType());

             try(InputStream inputStream = file.getInputStream()) {
                 amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata));
             } catch(IOException e) {
                 throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                         "파일 업로드에 실패했습니다.");
             }
             String fileUrl = amazonS3.getUrl(bucket, fileName).toString();
             log.info("편리한 이미지 정보 확인 로그 :"+fileName);
             fileUrlList.add(fileUrl);
         });

         return fileUrlList;
     }

    public void deleteImage(String fileName) {
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "파일 삭제에 실패하였습니다.");
        }
    }

    public String getFileName(String url) {
        if(url.contains(bucket)) {
            return url.substring(url.lastIndexOf('/') + 1);
        }

        return null;
    }

    public void deleteImageFromUrl(String url) {
        String fileName = getFileName(url);

        if(fileName != null) {
            deleteImage(fileName);
        }
    }
}