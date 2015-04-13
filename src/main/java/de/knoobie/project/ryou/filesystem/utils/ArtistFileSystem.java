package de.knoobie.project.ryou.filesystem.utils;

import de.knoobie.project.clannadutils.common.FileUtils;
import de.knoobie.project.clannadutils.common.NetUtils;
import de.knoobie.project.clannadutils.common.StringUtils;
import de.knoobie.project.fuko.database.domain.Artist;
import de.knoobie.project.fuko.database.domain.embeddable.AlbumLink;
import de.knoobie.project.fuko.database.domain.embeddable.Picture;
import de.knoobie.project.fuko.database.service.FukoDB;
import de.knoobie.project.ryou.filesystem.domain.FileOperationResult;
import de.knoobie.project.ryou.filesystem.domain.RyouPath;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ArtistFileSystem {

    private static final String PATH_ARTISTS = "I:\\ClannadProject\\Artist";
    private static final String FOLDER_NAME_FEATUREDON = "FeaturedOn";
    private static final String FOLDER_NAME_DISCOGRAPHY = "Discography";
    private static final String FOLDER_NAME_EXTRA = "Extra";

    public static boolean hasLocalFolder(Artist artist) {
        if (artist == null) {
            return false;
        }
        return FileUtils.exists(
                RyouPath.create(PATH_ARTISTS,
                        createFolderName(artist)).getPath());
    }

    public static String createFolderName(Artist artist) {
        return FileUtils.normalizeName(
                artist.getName() + " [" + artist.getVgmdbID() + "]");
    }

    public static Path createArtistDirectory(Artist artist) throws IOException {
        return FileUtils.createDirectory(RyouPath.create(PATH_ARTISTS,
                createFolderName(artist)).getPath());
    }

    public static Path getArtistDirectory(Artist artist, boolean createIfNotExists) throws IOException {
        if (createIfNotExists) {
            if (!hasLocalFolder(artist)) {
                return createArtistDirectory(artist);
            }
        }
        return RyouPath.create(PATH_ARTISTS, createFolderName(artist)).getPath();
    }

    public static FileOperationResult createCompleteArtistStructure(Artist artist, boolean force) {
        FileOperationResult result = new FileOperationResult();

        if (artist == null) {
            result.setSuccess(false);
            result.setMessage("Can't create a artist structure without an album.");
            return result;
        }

        Path artistDir;
        try {
            artistDir = getArtistDirectory(artist, true);
        } catch (IOException ex) {
            result.setSuccess(false);
            result.setMessage("Coudn't get ArtistDir -  IOExpcetion " + ex.getMessage());
            result.setException(ex);
            return result;
        }
        if (StringUtils.isEmpty(artist.getFolderName()) || force) {
            artist.setFolderName(createFolderName(artist));

            RyouPath picturePath = RyouPath.create(artistDir.toAbsolutePath().toString(), "Pictures");
            if (!FileUtils.exists(picturePath.getPath())) {
                try {
                    FileUtils.createDirectory(picturePath.getPath());
                } catch (IOException ex) {
                    result.getSubOperations().add(new FileOperationResult(
                            true, "Couldn't create directory '"
                            + picturePath.getPath().toAbsolutePath().toString() + "' IO Exception.", ex));
                }
            }
            RyouPath extraPath = RyouPath.create(artistDir.toAbsolutePath().toString(), FOLDER_NAME_EXTRA);
            if (!FileUtils.exists(extraPath.getPath())) {
                try {
                    FileUtils.createDirectory(extraPath.getPath());
                } catch (IOException ex) {
                    result.getSubOperations().add(new FileOperationResult(
                            true, "Couldn't create directory '"
                            + extraPath.getPath().toAbsolutePath().toString() + "' IO Exception.", ex));
                }
            }
            RyouPath discographyPath = RyouPath.create(artistDir.toAbsolutePath().toString(), FOLDER_NAME_DISCOGRAPHY);
            if (!FileUtils.exists(discographyPath.getPath())) {
                try {
                    FileUtils.createDirectory(discographyPath.getPath());
                } catch (IOException ex) {
                    result.getSubOperations().add(new FileOperationResult(
                            true, "Couldn't create directory '"
                            + discographyPath.getPath().toAbsolutePath().toString() + "' IO Exception.", ex));
                }
            }
            updateDiscography(artist, artist.getDiscography(), FOLDER_NAME_DISCOGRAPHY);
            RyouPath featuredOn = RyouPath.create(artistDir.toAbsolutePath().toString(), FOLDER_NAME_FEATUREDON);
            if (!FileUtils.exists(featuredOn.getPath())) {
                try {
                    FileUtils.createDirectory(featuredOn.getPath());
                } catch (IOException ex) {
                    result.getSubOperations().add(new FileOperationResult(
                            true, "Couldn't create directory '"
                            + featuredOn.getPath().toAbsolutePath().toString() + "' IO Exception.", ex));
                }
            }
            updateDiscography(artist, artist.getFeaturedOn(), FOLDER_NAME_FEATUREDON);

            if (artist.getPicture() != null) {
                Picture picture = artist.getPicture();
                try {
                    if (NetUtils.downloadFromUrl(StringUtils.isEmpty(picture.getUrlFull())
                            ? picture.getUrlSmall()
                            : picture.getUrlFull(),
                            artistDir.toAbsolutePath().toString()
                            + FileUtils.getFileSystem().getSeparator() + "artist.jpg")) {
                        picture.setPictureLocaleStorage(true);
                        picture.setPictureExtension("jpg");
                        picture.setPictureLocation("artist.jpg");
                    }
                } catch (IOException ex) {
                    result.getSubOperations().add(new FileOperationResult(
                            true, "Couldn't download artist.jpg IO Exception.", ex));
                }

            }

            FukoDB.getInstance().getArtistService().updateFileSystem(artist);
        }

        return result;
    }

    public static FileOperationResult updateDiscography(Artist artist, List<AlbumLink> albums, String folderName) {
        FileOperationResult result = new FileOperationResult();

        Path artistDir;
        try {
            artistDir = getArtistDirectory(artist, true);
        } catch (IOException ex) {
            result.setSuccess(false);
            result.setMessage("Coudn't get ArtistDir -  IOExpcetion " + ex.getMessage());
            result.setException(ex);
            return result;
        }

        for (AlbumLink link : albums) {
            try {
                Path albumDir = AlbumFileSystem.getAlbumDirectory(link, true);
                Files.createSymbolicLink(RyouPath.create(artistDir.toAbsolutePath().toString(),
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
