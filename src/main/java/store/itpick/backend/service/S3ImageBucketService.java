package store.itpick.backend.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import store.itpick.backend.common.exception.UserException;
import store.itpick.backend.dto.user.ProfileImgResponse;

import java.io.IOException;
import java.util.UUID;

import static store.itpick.backend.common.response.status.BaseExceptionResponseStatus.INVALID_PROFILE_IMG;
import static store.itpick.backend.common.response.status.BaseExceptionResponseStatus.UPLOAD_FAIL;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class S3ImageBucketService {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private String PROFILE_IMG_DIR = "profile/";
    private String DEBATE_IMG_DIR = "debate/";


    // 토론 이미지 업로드
    public String saveDebateImg(MultipartFile uploadFile){
        // img url 반환
        return saveImg(uploadFile, DEBATE_IMG_DIR);
    }

    // 프로필 이미지 업로드
    public String saveProfileImg(MultipartFile uploadFile) {
        // img url 반환
        return saveImg(uploadFile, PROFILE_IMG_DIR);
    }


    // 이미지 전처리
    private String saveImg(MultipartFile uploadFile, String profileImgDir) {
        if (uploadFile.isEmpty()) {
            log.debug("Upload file is empty");
            throw new UserException(INVALID_PROFILE_IMG);
        }
        String fileName = profileImgDir + UUID.randomUUID() + uploadFile.getOriginalFilename();

        ObjectMetadata metadata= new ObjectMetadata();
        metadata.setContentType(uploadFile.getContentType());
        metadata.setContentLength(uploadFile.getSize());

        return putS3(uploadFile, fileName, metadata);
    }


    // S3로 업로드
    private String putS3(MultipartFile uploadFile, String fileName, ObjectMetadata metadata) {

        // s3 업로드
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, fileName, uploadFile.getInputStream(), metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead);
            amazonS3Client.putObject(putObjectRequest);
        } catch (IOException e) {
            throw new UserException(UPLOAD_FAIL);
        }

        // url 반환
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }





}
