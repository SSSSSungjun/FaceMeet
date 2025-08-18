package com.levelup.FaceMeet.service.user;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private final AmazonS3 amazonS3;


    //esigned url 발급
    private String getPreSignedUrl(String fileName) {


        if (amazonS3.doesObjectExist(bucketName, fileName)) {
            amazonS3.deleteObject(bucketName, fileName);
            System.out.println("Deleted existing file with name: " + fileName);
        }

        GeneratePresignedUrlRequest generatePresignedUrlRequest = getGeneratePreSignedUrlRequest(fileName);
        URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

    //관상 검사시 한 사람당 3개의 presingedurl을 발급해준다
    public List<String> getPreSigned3Url(String fileName){
        String presingedUrl1 = getPreSignedUrl(fileName + "1");
        String presingedUrl2 = getPreSignedUrl(fileName +"2");
            String presingedUrl3 = getPreSignedUrl(fileName +"3");

        List<String> urls = new ArrayList<>();
        urls.add(presingedUrl1);
        urls.add(presingedUrl2);
        urls.add(presingedUrl3);

        return urls;
    }

    //파일 업로드용(PUT) presigned url 생성
    private GeneratePresignedUrlRequest getGeneratePreSignedUrlRequest(String fileName) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, fileName)
                        .withMethod(HttpMethod.PUT)
                        .withExpiration(getPreSignedUrlExpiration());
        generatePresignedUrlRequest.addRequestParameter(
                Headers.S3_CANNED_ACL,
                CannedAccessControlList.PublicRead.toString());
        return generatePresignedUrlRequest;
    }

    //presigned url 유효 기간 설정
    private Date getPreSignedUrlExpiration() {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 2;
        expiration.setTime(expTimeMillis);
        return expiration;
    }

}