package de.knoobie.project.ryou.filesystem.utils;

import de.knoobie.project.clannadutils.common.FileUtils;
import de.knoobie.project.clannadutils.common.NetUtils;
import de.knoobie.project.clannadutils.common.StringUtils;
import de.knoobie.project.fuko.database.domain.Album;
import de.knoobie.project.fuko.database.domain.AlbumDisc;
import de.knoobie.project.fuko.database.domain.embeddable.AlbumLink;
import de.knoobie.project.fuko.database.service.FukoDB;
import de.knoobie.project.ryou.filesystem.domain.FileOperationResult;
import de.knoobie.project.ryou.filesystem.domain.RyouPath;
import java.io.IOException;
import java.nio.file.Path;

public class AlbumFileSystem {

    private static final String PATH_ALBUMS = "I:\\ClannadProject\\Album";
    private static final String FOLDER_NAME_EXTRA = "Extra";

    public static boolean hasLocalFolder(Album album) {
        if (album == null) {
            return false;
        }
        return FileUtils.exists(
                RyouPath.create(PATH_ALBUMS,
                        createFolderName(album)).getPath());
    }

    public static boolean hasLocalFolder(AlbumLink album) {
        if (album == null) {
            return false;
        }
        return FileUtils.exists(
                RyouPath.create(PATH_ALBUMS,
                        createFolderName(album)).getPath());
    }

    public static String createFolderName(Album album) {
        return FileUtils.normalizeName(
                album.getName() + " [" + album.getVgmdbID() + "]");
    }

    public static String createFolderName(AlbumLink album) {
        return FileUtils.normalizeName(
                album.getPrimaryName().getName() + " [" + album.getVgmdbID() + "]");
    }

    public static Path createAlbumDirectory(Album album) throws IOException {
        return FileUtils.createDirectory(RyouPath.create(PATH_ALBUMS,
                createFolderName(album)).getPath());
    }

    public static Path createAlbumDirectory(AlbumLink album) throws IOException {
        return FileUtils.createDirectory(RyouPath.create(PATH_ALBUMS,
                createFolderName(album)).getPath());
    }

    public static Path getAlbumDirectory(AlbumLink album, boolean createIfNotExists) throws IOException {
        if (createIfNotExists) {
            if (!hasLocalFolder(album)) {
                return createAlbumDirectory(album);
            }
        }
        return RyouPath.create(PATH_ALBUMS, createFolderName(album)).getPath();
    }

    public static Path getAlbumDirectory(Album album, boolean createIfNotExists) throws IOException {
        if (createIfNotExists) {
            if (!hasLocalFolder(album)) {
                return createAlbumDirectory(album);
            }
        }
        return RyouPath.create(PATH_ALBUMS, createFolderName(album)).getPath();
    }

    public static FileOperationResult createCompleteAlbumStructure(Album album, boolean force) {
        FileOperationResult result = new FileOperationResult();

        if (album == null) {
            result.setSuccess(false);
            result.setMessage("Can't create a album structure without an album.");
            return result;
        }

        Path albumDir;
        try {
            albumDir = getAlbumDirectory(album, true);
        } catch (IOException ex) {
            result.setSuccess(false);
            result.setMessage("Coudn't get AlbumDir -  IOExpcetion " + ex.getMessage());
            result.setException(ex);
            return result;
        }
        if (StringUtils.isEmpty(album.getFolderName()) || force) {
            album.setFolderName(createFolderName(album));

            int discNumber = 0;

            for (AlbumDisc disc : album.getDiscs()) {
                discNumber++;
                RyouPath ryouPath = RyouPath.create(albumDir.toAbsolutePath().toString(), "Disc" + discNumber);
                if (!FileUtils.exists(ryouPath.getPath())) {
                    try {
                        FileUtils.createDirectory(ryouPath.getPath());
                    } catch (IOException ex) {
                        result.getSubOperations().add(new FileOperationResult(
                                true, "Couldn't create directory '"
                                + ryouPath.getPath().toAbsolutePath().toString() + "' IO Exception.", ex));
                    }
                }
            }

            RyouPath scanDir = RyouPath.create(albumDir.toAbsolutePath().toString(), "Scans");
            if (!FileUtils.exists(scanDir.getPath())) {
                try {
                    FileUtils.createDirectory(scanDir.getPath());
                } catch (IOException ex) {
                    result.getSubOperations().add(new FileOperationResult(
                            true, "Couldn't create directory '"
                            + scanDir.getPath().toAbsolutePath().toString() + "' IO Exception.", ex));
                }
            }
            RyouPath extraPath = RyouPath.create(albumDir.toAbsolutePath().toString(), FOLDER_NAME_EXTRA);
            if (!FileUtils.exists(extraPath.getPath())) {
                try {
                    FileUtils.createDirectory(extraPath.getPath());
                } catch (IOException ex) {
                    result.getSubOperations().add(new FileOperationResult(
                            true, "Couldn't create directory '"
                            + extraPath.getPath().toAbsolutePath().toString() + "' IO Exception.", ex));
                }
            }

            album.getPictures().stream().forEach((picture) -> {
                if (picture.isCover()) {
                    try {
                        if (NetUtils.downloadFromUrl(StringUtils.isEmpty(picture.getUrlFull())
                                ? picture.getUrlSmall()
                                : picture.getUrlFull(),
                                albumDir.toAbsolutePath().toString()
                                + FileUtils.getFileSystem().getSeparator() + "cover.jpg")) {
                            picture.setPictureLocaleStorage(true);
                            picture.setPictureExtension("jpg");
                            picture.setPictureLocation("cover.jpg");
                        }
                    } catch (IOException ex) {
                        result.getSubOperations().add(new FileOperationResult(
                                true, "Couldn't download cover IO Exception.", ex));
                    }
                } else {
                    try {
                        if (NetUtils.downloadFromUrl(StringUtils.isEmpty(picture.getUrlFull())
                                ? picture.getUrlSmall()
                                : picture.getUrlFull(),
                                albumDir.toAbsolutePath().toString()
                                + FileUtils.getFileSystem().getSeparator()
                                + "Scans"
                                + FileUtils.getFileSystem().getSeparator() + FileUtils.normalizeName(picture.getPictureName()) + ".jpg")) {
                            picture.setPictureLocaleStorage(true);
                            picture.setPictureExtension("jpg");
                            picture.setPictureLocation(FileUtils.normalizeName(picture.getPictureName()) + ".jpg");
                        }
                    } catch (IOException ex) {
                        result.getSubOperations().add(new FileOperationResult(
                                true, "Couldn't download cover IO Exception.", ex));
                    }
                }
            });
            FukoDB.getInstance().getAlbumService().updateFileSystem(album);
        }

        return result;
    }

}
