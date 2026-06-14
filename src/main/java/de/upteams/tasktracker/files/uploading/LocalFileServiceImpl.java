package de.upteams.tasktracker.files.uploading;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local")
public class LocalFileServiceImpl implements FileService {

    @Value("${app.storage.local.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public CompletableFuture<Boolean> uploadFileAsync(
            String objectKey,
            InputStream inputStream,
            Map<String, String> metadata,
            String contentType,
            Long contentLength,
            boolean isPublicRead
    ) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new IllegalArgumentException("Object key cannot be null or empty");
        }

        if (contentLength == null || contentLength <= 0) {
            throw new IllegalArgumentException(
                    String.format("Invalid content length %d for '%s'", contentLength, objectKey)
            );
        }

        try (inputStream) {
            Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path targetPath = basePath.resolve(objectKey).normalize();

            if (!targetPath.startsWith(basePath)) {
                throw new IllegalArgumentException("Invalid object key path");
            }

            Files.createDirectories(targetPath.getParent());
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Local file uploaded successfully: {}", targetPath);

            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("Local file upload failed. objectKey={}", objectKey, e);
            return CompletableFuture.completedFuture(false);
        }
    }
}