package com.cywedding.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;

import com.cywedding.common.DMLType;
import com.cywedding.dto.Image;
import com.cywedding.dto.QRGroup;
import com.cywedding.dto.QRUser;
import com.cywedding.mapper.ImageMapper;

import lombok.RequiredArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ImageService {

    @Autowired
    private QRUserService userService;

    @Autowired
    private QRGroupService groupService;

    @Autowired
    private JavaMailSender mailSender;

    private final ImageMapper imageMapper;

    @Transactional
    public void uploadImage(Image image) {
        imageMapper.uploadImage(image);

        QRUser user = userService.fetchQRUser(image.getGroupName(), image.getQrCode());

        QRUser param = new QRUser();
        param.setGroupId(user.getCustomGroupId());
        param.setQrCode(user.getQrCode());
        param.setType(DMLType.UPLOAD.name());

        userService.updateUser(param);
    }

    public List<Image> selectImageList(QRUser user, String plan) {
        Image image = new Image();
        image.setGroupId(user.getGroupId());
        image.setQrCode(user.getQrCode());
        image.setPlan(plan);

        return imageMapper.selectImageList(image);
    }

    // 관리자 전용
    @Transactional
    public void deleteImage(String domain, String code, String fileName) {
        Image image = fetchImage(fileName);
        QRUser user = userService.fetchQRUser(domain, code);

        QRUser param = new QRUser();
        param.setGroupId(user.getGroupId());
        param.setQrCode(image.getQrCode());
        param.setType(DMLType.UPLOAD_CANCEL.name());

        userService.updateUser(param);
        imageMapper.deleteImageByImage(image);
    }

    public void deleteImage(QRUser user) {
        imageMapper.deleteImageByUser(user);
    }

    public Image fetchImage(String fileName) {
        Image image = imageMapper.selectImage(fileName);
        if (image == null) {
            throw new IllegalArgumentException("이미지가 존재하지 않습니다: " + fileName);
        }
        return image;
    }

    @Async
    public void sendEmail(String domain, String plan, String emailAddress) throws MessagingException, IOException {
        QRGroup group = groupService.fetchQRGroup(domain);

        Image param = new Image(true);
        param.setGroupId(group.getGroupId());
        param.setPlan(plan);
        List<Image> imageList = imageMapper.selectImageList(param);

        ByteArrayOutputStream zipOutStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(zipOutStream);

        for (Image image : imageList) {
            String fileName = image.getFileName();
            String imageUrl = image.getImageUrl();

            try {
                URI uri = URI.create(imageUrl);
                try (InputStream inputStream = uri.toURL().openStream()) {
                    ZipEntry entry = new ZipEntry(fileName);

                    zip.putNextEntry(entry);
                    inputStream.transferTo(zip);
                    zip.closeEntry();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        zip.close();

        byte[] zipBytes = zipOutStream.toByteArray();

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        List<String> emailList = Arrays.stream(emailAddress.split(","))
                                         .map(String::trim)
                                         .filter(email -> !email.isEmpty())
                                         .collect(Collectors.toList());

        helper.setTo(emailList.toArray(new String[0]));
        helper.setFrom("xsonyn14@gmail.com");
        helper.setSubject("웨딩 이미지 첨부파일");
        helper.setText(
            """
            <div style="font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; font-size: 15px; line-height: 1.6; color: #333;">
                <p>안녕하세요, 고객님.</p>
                <p>
                    소중한 순간을 함께해 주셔서 <strong>진심으로 감사드립니다.</strong><br>
                    촬영된 <span style="color: #007bff; font-weight: bold;">웨딩 이미지</span>를 ZIP 파일로 첨부해드리오니, 
                    아래 첨부파일을 확인 부탁드립니다.
                </p>
                <p>
                    이번 <em>Photo Event</em>가 두 분의 결혼식을 더욱 특별하게 기록하는 
                    시간이 되었기를 바랍니다.
                </p>
                <p>
                    앞으로도 변함없는 <strong style="color: #e83e8c;">🌸 행복과 💑 사랑</strong>이 가득하시길 기원합니다.
                </p>
                <p>감사합니다.</p>

                <hr style="border: none; border-top: 1px solid #ddd; margin: 20px 0;">
                <p style="text-align: right;">
                    💕 <strong>송감자 & 임감자</strong> 드림
                </p>
            </div>
            """,
            true
        );

        ByteArrayResource zipResource = new ByteArrayResource(zipBytes);
        helper.addAttachment("images.zip", zipResource);

        mailSender.send(message);
    }
}