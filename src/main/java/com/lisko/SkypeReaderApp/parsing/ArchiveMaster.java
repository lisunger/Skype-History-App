package com.lisko.SkypeReaderApp.parsing;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

/**
 * @author n.kanev
 * Class that performs the archive unpack and copy operations with the skype history export.
 * Can:
 * <ul>
 *     <li>Save the .tar archive to a temp folder</li>
 *     <li>Unpack the archive contents to the temp folder</li>
 *     <li>Delete the archive</li>
 *     <li>Delete the unpacked files</li>
 *     <
 * </ul>
 */
public class ArchiveMaster {

    public static final String TEMP_FOLDER_STRING = "C:/lisko/temp/";
    private static final Path tempFolderPath = Paths.get(TEMP_FOLDER_STRING);

    private final UploadedFile uploadedFile;
    private final String uploadedFileName;
    private File archiveCopyFile;

    public ArchiveMaster(FileUploadEvent fileUploadEvent) {
        this.uploadedFile = fileUploadEvent.getFile();
        this.uploadedFileName = fileUploadEvent.getFile().getFileName();
    }

    public void unpackFiles() throws IOException {
        // Prepare folder
        this.createTempFolder();

        // Copy file to temp file
        this.copyArchiveToTemp();

        // Unpack archive
        this.unpackTarToTemp();

        // Delete .tar file, no longer needed
        this.deleteCopiedArchive();
    }

    /**
     * Create a temp folder at the location of {@link ArchiveMaster#TEMP_FOLDER_STRING}.
     * If the folder already exists, the contents are removed first.
     * @return Path to the temp folder.
     * @throws IOException
     */
    private void createTempFolder() throws IOException {
        if(tempFolderPath.toFile().exists()) {
            Files.walk(tempFolderPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        tempFolderPath.toFile().mkdirs();
        this.archiveCopyFile = new File(tempFolderPath.toFile(), this.uploadedFile.getFileName());
    }

    private void copyArchiveToTemp() throws IOException {
        this.archiveCopyFile = new File(tempFolderPath.toFile(), this.uploadedFileName);
        Files.copy(this.uploadedFile.getInputStream(), this.archiveCopyFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void unpackTarToTemp() throws IOException {
        TarArchiveInputStream tarArchiveStream = new TarArchiveInputStream(this.uploadedFile.getInputStream());
        TarArchiveEntry tarEntry = tarArchiveStream.getNextTarEntry();
        while (tarEntry != null) {
            File destPath = new File(tempFolderPath.toFile(), tarEntry.getName());

            if(tarEntry.isDirectory()) {
                //System.out.println("Directory \t\t" + tarEntry.getName() + "\t\t\t" + tarEntry.getFile());
                destPath.mkdirs();
            }
            else {
                //System.out.println("File \t\t" + tarEntry.getName() + "\t\t\t" + tarEntry.getFile());
                destPath.createNewFile();
                byte[] btoRead = new byte[2048];
                BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(destPath));
                int len;
                while((len = tarArchiveStream.read(btoRead)) != -1) {
                    bout.write(btoRead, 0, len);
                }

                bout.close();
            }
            tarEntry = tarArchiveStream.getNextTarEntry();
        }
        tarArchiveStream.close();
    }

    private void deleteCopiedArchive() throws IOException {
        Files.delete(this.archiveCopyFile.toPath());
    }

    public static void cleanupTempFolder() throws IOException {
        System.out.println("Cleaning up...");
        Files.walk(Paths.get(TEMP_FOLDER_STRING).getParent())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public static Path getTempFolderPath() {
        return tempFolderPath;
    }

}
