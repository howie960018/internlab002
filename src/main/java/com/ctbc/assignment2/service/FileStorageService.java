package com.ctbc.assignment2.service;

import com.ctbc.assignment2.exception.InvalidFileException;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String store(MultipartFile file) throws InvalidFileException;
}
