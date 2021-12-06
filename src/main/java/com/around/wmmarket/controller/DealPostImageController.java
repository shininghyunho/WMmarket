package com.around.wmmarket.controller;

import com.around.wmmarket.controller.dto.DealPostImage.DealPostImageSaveRequestDto;
import com.around.wmmarket.domain.deal_post_image.DealPostImage;
import com.around.wmmarket.domain.user.SignedUser;
import com.around.wmmarket.service.dealPost.DealPostService;
import com.around.wmmarket.service.dealPostImage.DealPostImageService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@Slf4j
@RequiredArgsConstructor
@RestController
public class DealPostImageController {
    private final DealPostImageService dealPostImageService;
    private final DealPostService dealPostService;
    private final ResourceLoader resourceLoader;
    private final Tika tika=new Tika();

    @PostMapping("/api/v1/dealPostImage")
    public ResponseEntity<?> save(@AuthenticationPrincipal SignedUser signedUser, @ModelAttribute DealPostImageSaveRequestDto requestDto) throws Exception{
        // signedUser 와 dealPostId 의 email 비교
        if(!dealPostService.isDealPostAuthor(signedUser,requestDto.getDealPostId())){
            return ResponseEntity.badRequest().body("게시글의 작성자가 아닙니다.");
        }
        dealPostImageService.save(requestDto.getDealPostId(),requestDto.getFiles());
        return ResponseEntity.ok().body("save success");
    }

    @GetMapping("/api/v1/dealPostImage")
    public ResponseEntity<?> get(@RequestParam Integer dealPostImageId) throws Exception{
        DealPostImage dealPostImage=dealPostImageService.get(dealPostImageId);
        // 절대경로
        String absPath=new File("").getAbsolutePath()+File.separator;
        // 저장할 세부경로
        String resourcePath="src"+File.separator
                +"main"+File.separator
                +"resources"+File.separator;

        String filePath="images"+File.separator
                +"dealPostImages"+File.separator;
        String fileName=dealPostImage.getName();
        String path=filePath+fileName;
        log.info("path:"+path);
        Resource resource=resourceLoader.getResource("file:"+absPath+resourcePath+path);
        File file=resource.getFile();
        String mediaType=tika.detect(file);

        HttpHeaders headers=new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+resource.getFilename()+"\"");
        headers.add(HttpHeaders.CONTENT_TYPE,mediaType);
        headers.add(HttpHeaders.CONTENT_LENGTH,String.valueOf(file.length()));

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @GetMapping("/test/file")
    public ResponseEntity<?> testFile(@RequestParam String name) throws Exception{
        //Resource resource=resourceLoader.getResource("classpath:"+name);
        // 절대경로
        String absPath=new File("").getAbsolutePath()+File.separator;
        // 저장할 세부경로
        String resourcePath="src"+File.separator
                +"main"+File.separator
                +"resources"+File.separator;
        Resource resource=resourceLoader.getResource("file:"+absPath+resourcePath+name);
        log.info("path:"+name);
        File file=resource.getFile();
        String mediaType=tika.detect(file);

        HttpHeaders headers=new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+resource.getFilename()+"\"");
        headers.add(HttpHeaders.CONTENT_TYPE,mediaType);
        headers.add(HttpHeaders.CONTENT_LENGTH,String.valueOf(file.length()));

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}