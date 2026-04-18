package com.ctbc.assignment2;

import com.ctbc.assignment2.exception.InvalidFileException;
import com.ctbc.assignment2.service.impl.FileStorageServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FileStorageServiceImplTest {

    @Test
    public void testStoreJpegSuccess() throws IOException {
        FileStorageServiceImpl service = new FileStorageServiceImpl();
        Path dir = Paths.get("uploads/test");
        Files.createDirectories(dir);
        ReflectionTestUtils.setField(service, "uploadDir", "uploads/test");

        MockMultipartFile file = new MockMultipartFile(
                "coverImage",
                "cover.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );

        String url = service.store(file);
        assertThat(url).startsWith("/uploads/test/");
        String filename = url.substring("/uploads/test/".length());
        assertThat(Files.exists(dir.resolve(filename))).isTrue();
    }

    @Test
    public void testStoreRejectsInvalidType() {
        FileStorageServiceImpl service = new FileStorageServiceImpl();
        ReflectionTestUtils.setField(service, "uploadDir", "uploads/test");

        MockMultipartFile file = new MockMultipartFile(
                "coverImage",
                "cover.gif",
                "image/gif",
                new byte[]{1, 2, 3}
        );

        assertThrows(InvalidFileException.class, () -> service.store(file));
    }

    @Test
    public void testStoreRejectsLargeFile() {
        FileStorageServiceImpl service = new FileStorageServiceImpl();
        ReflectionTestUtils.setField(service, "uploadDir", "uploads/test");

        byte[] big = new byte[2 * 1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile(
                "coverImage",
                "cover.jpg",
                "image/jpeg",
                big
        );

        assertThrows(InvalidFileException.class, () -> service.store(file));
    }
}
