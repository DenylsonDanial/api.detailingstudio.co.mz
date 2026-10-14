package mz.co.amaterasu.gestao.util;

import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {

    private FileUtil() {}

    public static File getFileFromResources(String fileName) throws IOException {
        ClassPathResource resource = new ClassPathResource(fileName);
        File tempFile = File.createTempFile("pgc", ".xlsx");

        try (InputStream is = resource.getInputStream();
             FileOutputStream fos = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
        return tempFile;
    }
}
