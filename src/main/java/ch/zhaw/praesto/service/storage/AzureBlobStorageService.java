package ch.zhaw.praesto.service.storage;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.net.URLConnection;

/**
 * Speichert Dateien in Azure Blob Storage. Aktiv, sobald {@code praesto.storage.type=azure}
 * gesetzt ist. Benötigt {@code praesto.storage.azure.connection-string} (Azure-App-Einstellung
 * AZURE_STORAGE_CONNECTION_STRING) und optional {@code praesto.storage.azure.container}.
 *
 * Der Blob-Name entspricht 1:1 dem bisherigen Dateinamen (dem in der DB gespeicherten
 * {@code fileUrl}) – die Datenbank muss bei einer Migration also NICHT geändert werden.
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "praesto.storage.type", havingValue = "azure")
public class AzureBlobStorageService implements StorageService {

    private final BlobContainerClient container;

    public AzureBlobStorageService(
            @Value("${praesto.storage.azure.connection-string}") String connectionString,
            @Value("${praesto.storage.azure.container:documents}") String containerName) {
        BlobServiceClient service = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        this.container = service.getBlobContainerClient(containerName);
        if (!container.exists()) {
            container.createIfNotExists();
        }
        log.info("Storage: AZURE Blob Storage, Container '{}'", containerName);
    }

    private BlobClient blob(String storedName) {
        return container.getBlobClient(storedName);
    }

    @Override
    public void store(String storedName, byte[] content) {
        BlobClient b = blob(storedName);
        b.upload(BinaryData.fromBytes(content), true);
        String ct = URLConnection.guessContentTypeFromName(storedName);
        if (ct != null) {
            b.setHttpHeaders(new BlobHttpHeaders().setContentType(ct));
        }
    }

    @Override
    public boolean exists(String storedName) {
        return blob(storedName).exists();
    }

    @Override
    public byte[] readAllBytes(String storedName) {
        BlobClient b = blob(storedName);
        if (!b.exists()) {
            return null;
        }
        return b.downloadContent().toBytes();
    }

    @Override
    public Resource load(String storedName) {
        byte[] bytes = readAllBytes(storedName);
        return bytes == null ? null : new ByteArrayResource(bytes);
    }

    @Override
    public String probeContentType(String storedName) {
        try {
            String ct = blob(storedName).getProperties().getContentType();
            if (ct != null && !ct.isBlank()) {
                return ct;
            }
        } catch (Exception ignored) {
            // Fallback unten
        }
        return URLConnection.guessContentTypeFromName(storedName);
    }

    @Override
    public void delete(String storedName) {
        blob(storedName).deleteIfExists();
    }
}
