package de.knoobie.project.ryou.filesystem.utils;

import de.knoobie.project.clannadutils.common.FileUtils;
import de.knoobie.project.clannadutils.common.NetUtils;
import de.knoobie.project.clannadutils.common.StringUtils;
import de.knoobie.project.fuko.database.domain.Organization;
import de.knoobie.project.fuko.database.domain.Product;
import de.knoobie.project.fuko.database.domain.embeddable.AlbumLink;
import de.knoobie.project.fuko.database.domain.embeddable.OrganizationLink;
import de.knoobie.project.fuko.database.domain.embeddable.Picture;
import de.knoobie.project.fuko.database.domain.embeddable.ProductLink;
import de.knoobie.project.fuko.database.service.FukoDB;
import de.knoobie.project.ryou.filesystem.domain.FileOperationResult;
import de.knoobie.project.ryou.filesystem.domain.RyouPath;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class OrganizationFileSystem {

    private static final String PATH_ORGANIZATIONS = "I:\\ClannadProject\\Organization";
    private static final String FOLDER_NAME_ALBUMS = "Albums";
    private static final String FOLDER_NAME_EXTRA = "Extra";

    public static boolean hasLocalFolder(OrganizationLink org) {
        if (org == null) {
            return false;
        }
        return FileUtils.exists(
                RyouPath.create(PATH_ORGANIZATIONS,
                        createFolderName(org)).getPath());
    }

    public static String createFolderName(OrganizationLink org) {
        return FileUtils.normalizeName(
                org.getPrimaryName().getName() + " [" + org.getVgmdbID() + "]");
    }

    public static Path createOrganzationDirectory(OrganizationLink org) throws IOException {
        return FileUtils.createDirectory(RyouPath.create(PATH_ORGANIZATIONS,
                createFolderName(org)).getPath());
    }

    public static Path getOrganizationDirectory(OrganizationLink org, boolean createIfNotExists) throws IOException {
        if (createIfNotExists) {
            if (!hasLocalFolder(org)) {
                return createOrganzationDirectory(org);
            }
        }
        return RyouPath.create(PATH_ORGANIZATIONS, createFolderName(org)).getPath();
    }

    public static boolean hasLocalFolder(Organization org) {
        if (org == null) {
            return false;
        }
        return FileUtils.exists(
                RyouPath.create(PATH_ORGANIZATIONS,
                        createFolderName(org)).getPath());
    }

    public static String createFolderName(Organization org) {
        return FileUtils.normalizeName(
                org.getName() + " [" + org.getVgmdbID() + "]");
    }

    public static Path createOrganizationDirectory(Organization org) throws IOException {
        return FileUtils.createDirectory(RyouPath.create(PATH_ORGANIZATIONS,
                createFolderName(org)).getPath());
    }

    public static Path getOrganizationDirectory(Organization org, boolean createIfNotExists) throws IOException {
        if (createIfNotExists) {
            if (!hasLocalFolder(org)) {
                return createOrganizationDirectory(org);
            }
        }
        return RyouPath.create(PATH_ORGANIZATIONS, createFolderName(org)).getPath();
    }

    public static FileOperationResult createCompleteOrganizationStructure(Organization org, boolean force) {
        FileOperationResult result = new FileOperationResult();

        if (org == null) {
            result.setSuccess(false);
            result.setMessage("Can't create a artist structure without an album.");
            return result;
        }

        Path orgDir;
        try {
            orgDir = getOrganizationDirectory(org, true);
        } catch (IOException ex) {
            result.setSuccess(false);
            result.setMessage("Coudn't get productDir -  IOExpcetion " + ex.getMessage());
            result.setException(ex);
            return result;
        }
        if (StringUtils.isEmpty(org.getFolderName()) || force) {
            org.setFolderName(createFolderName(org));

            RyouPath picturePath = RyouPath.create(orgDir.toAbsolutePath().toString(), "Pictures");
            if (!FileUtils.exists(picturePath.getPath())) {
                try {
                    FileUtils.createDirectory(picturePath.getPath());
                } catch (IOException ex) {
                    result.getSubOperations().add(new FileOperationResult(
                            true, "Couldn't create directory '"
                            + picturePath.getPath().toAbsolutePath().toString() + "' IO Exception.", ex));
                }
            }
            RyouPath extraPath = RyouPath.create(orgDir.toAbsolutePath().toString(), FOLDER_NAME_EXTRA);
            if (!FileUtils.exists(extraPath.getPath())) {
                try {
                    FileUtils.createDirectory(extraPath.getPath());
                } catch (IOException ex) {
                    result.getSubOperations().add(new FileOperationResult(
                            true, "Couldn't create directory '"
                            + extraPath.getPath().toAbsolutePath().toString() + "' IO Exception.", ex));
                }
            }
            RyouPath discographyPath = RyouPath.create(orgDir.toAbsolutePath().toString(), FOLDER_NAME_ALBUMS);
            if (!FileUtils.exists(discographyPath.getPath())) {
                try {
                    FileUtils.createDirectory(discographyPath.getPath());
                } catch (IOException ex) {
                    result.getSubOperations().add(new FileOperationResult(
                            true, "Couldn't create directory '"
                            + discographyPath.getPath().toAbsolutePath().toString() + "' IO Exception.", ex));
                }
            }
            updateRelatedAlbums(org, org.getReleases(), FOLDER_NAME_ALBUMS);

            if (org.getPicture() != null) {
                Picture picture = org.getPicture();
                try {
                    if (NetUtils.downloadFromUrl(StringUtils.isEmpty(picture.getUrlFull())
                            ? picture.getUrlSmall()
                            : picture.getUrlFull(),
                            orgDir.toAbsolutePath().toString()
                            + FileUtils.getFileSystem().getSeparator() + "organization.jpg")) {
                        picture.setPictureLocaleStorage(true);
                        picture.setPictureExtension("jpg");
                        picture.setPictureLocation("organization.jpg");
                    }
                } catch (IOException ex) {
                    result.getSubOperations().add(new FileOperationResult(
                            true, "Couldn't download artist.jpg IO Exception.", ex));
                }

            }

            FukoDB.getInstance().getOrganizationService().updateFileSystem(org);
        }

        return result;
    }

    public static FileOperationResult updateRelatedAlbums(Organization org, List<AlbumLink> albums, String folderName) {
        FileOperationResult result = new FileOperationResult();

        Path orgDir;
        try {
            orgDir = getOrganizationDirectory(org, true);
        } catch (IOException ex) {
            result.setSuccess(false);
            result.setMessage("Coudn't get orgDir -  IOExpcetion " + ex.getMessage());
            result.setException(ex);
            return result;
        }

        for (AlbumLink link : albums) {
            try {
                Path albumDir = AlbumFileSystem.getAlbumDirectory(link, true);
                Files.createSymbolicLink(RyouPath.create(orgDir.toAbsolutePath().toString(),
                        folderName,
                        AlbumFileSystem.createFolderName(link)).getPath(),
                        albumDir);
            } catch (FileAlreadyExistsException ex) {
                ex.printStackTrace();
                result.getSubOperations().add(new FileOperationResult(
                        true, "Couldn't create link FileAlreadyExistsException Exception.", ex));
            } catch (IOException ex) {
                ex.printStackTrace();
                result.getSubOperations().add(new FileOperationResult(
                        true, "Couldn't create link IO Exception.", ex));
            }

        }

        return result;
    }

}
