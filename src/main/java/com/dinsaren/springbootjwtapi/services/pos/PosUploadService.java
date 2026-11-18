package com.dinsaren.springbootjwtapi.services.pos;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface PosUploadService {
    Map<String, String> uploadFile(MultipartFile file);
}
