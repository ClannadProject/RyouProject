package de.knoobie.project.ryou.filesystem.utils;

import de.knoobie.project.clannadutils.common.FileUtils;
import de.knoobie.project.clannadutils.common.NetUtils;
import de.knoobie.project.clannadutils.common.StringUtils;
import de.knoobie.project.fuko.database.domain.Product;
import de.knoobie.project.fuko.database.domain.embeddable.AlbumLink;
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

public class ProductFileSystem {

    private static final String PATH_PRODUCTS = "I:\\ClannadProject\\Product";
    private static final String FOLDER_NAME_ALBUMS = "Albums";
    private static final String FOLDER_NAME_TITLES = "Titles";
    private static final String FOLDER_NAME_FRANCHISES = "Franchises";
    private static final String FOLDER_NAME_EXTRA = "Extra";

    public static boolean hasLocalFolder(ProductLink product) {
        if (product == null) {
            return false;
        }
        return FileUtils.exists(
                RyouPath.create(PATH_PRODUCTS,
                        createFolderName(product)).getPath());
    }

    public static String createFolderName(ProductLink product) {
        return FileUtils.normalizeName(
                product.getPrimaryName().getName() + " [" + product.getVgmdbID() + "]");
    }

    public static Path createProductDirectory(ProductLink product) throws IOException {
        return FileUtils.createDirectory(RyouPath.create(PATH_PRODUCTS,
                createFolderName(product)).getPath());
    }

    public static Path getProductDirectory(ProductLink product, boolean createIfNotExists) throws IOException {
        if (createIfNotExists) {
            if (!hasLocalFolder(product)) {
                return createProductDirectory(product);
            }
        }
        return RyouPath.create(PATH_PRODUCTS, createFolderName(product)).getPath();
    }

    public static boolean hasLocalFolder(Product product) {
        if (product == null) {
            return false;
        }
        return FileUtils.exists(
                RyouPath.create(PATH_PRODUCTS,
                        createFolderName(product)).getPath());
    }

    public static String createFolderName(Product product) {
        return FileUtils.normalizeName(
                product.getName() + " [" + product.getVgmdbID() + "]");
    }

    public static Path createProductDirectory(Product product) throws IOException {
        return FileUtils.createDirectory(RyouPath.create(PATH_PRODUCTS,
                createFolderName(product)).getPath());
    }

    public static Path getProductDirectory(Product product, boolean createIfNotExists) throws IOException {
        if (createIfNotExists) {
            if (!hasLocalFolder(product)) {
                return createProductDirectory(product);
            }
        }
        return RyouPath.create(PATH_PRODUCTS, createFolderName(product)).getPath();
    }

    public static FileOperationResult createCompleteProductStructure(Product product, boolean force) {
        FileOperationResult result = new FileOperationResult();

        if (product == null) {
            result.setSuccess(false);
            result.setMessage("Can't create a artist structure without an album.");
            return result;
        }

        Path productDir;
        try {
            productDir = getProductDirectory(product, true);
        } catch (IOException ex) {
            result.setSuccess(false);
            result.setMessage("Coudn't get productDir -  IOExpcetion " + ex.getMessage());
            result.setException(ex);
            return result;
        }
        if (StringUtils.isEmpty(product.getFolderName()) || force) {
            product.setFolderName(createFolderName(product));

            RyouPath picturePath = RyouPath.create(productDir.toAbsolutePath().toString(), "Pictures");
            if (!FileUtils.exists(picturePath.getPath())) {
                try {
                    FileUtils.createDirectory(picturePath.getPath());
                } catch (IOException ex) {
                    result.getSubOperations().add(new FileOperationResult(
                            true, "Couldn't create directory '"
                            + picturePath.getPath().toAbsolutePath().toString() + "' IO Exception.", ex));
                }
            }
            RyouPath extraPath = RyouPath.create(productDir.toAbsolutePath().toString(), FOLDER_NAME_EXTRA);
            if (!FileUtils.exists(extraPath.getPath())) {
                try {
                    FileUtils.createDirectory(extraPath.getPath());
                } catch (IOException ex) {
                    result.getSubOperations().add(new FileOperationResult(
                            true, "Couldn't create directory '"
                            + extraPath.getPath().toAbsolutePath().toString() + "' IO Exception.", ex));
                }
            }
            RyouPath discographyPath = RyouPath.create(productDir.toAbsolutePath().toString(), FOLDER_NAME_ALBUMS);
            if (!FileUtils.exists(discographyPath.getPath())) {
                try {
                    FileUtils.createDirectory(discographyPath.getPath());
                } catch (IOException ex) {
                    result.getSubOperations().add(new FileOperationResult(
                            true, "Couldn't create directory '"
                            + discographyPath.getPath().toAbsolutePath().toString() + "' IO Exception.", ex));
                }
            }
            updateRelatedAlbums(product, product.getRelatedAlbums(), FOLDER_NAME_ALBUMS);
            RyouPath titles = RyouPath.create(productDir.toAbsolutePath().toString(), FOLDER_NAME_TITLES);
            if (!FileUtils.exists(titles.getPath())) {
                try {
                    FileUtils.createDirectory(titles.getPath());
                } catch (IOException ex) {
                    result.getSubOperations().add(new FileOperationResult(
                            true, "Couldn't create directory '"
                            + titles.getPath().toAbsolutePath().toString() + "' IO Exception.", ex));
                }
            }
            updateRelatedProducts(product, product.getTitles(), FOLDER_NAME_TITLES);
            RyouPath franchises = RyouPath.create(productDir.toAbsolutePath().toString(), FOLDER_NAME_FRANCHISES);
            if (!FileUtils.exists(franchises.getPath())) {
                try {
                    FileUtils.createDirectory(franchises.getPath());
                } catch (IOException ex) {
                    result.getSubOperations().add(new FileOperationResult(
                            true, "Couldn't create directory '"
                            + franchises.getPath().toAbsolutePath().toString() + "' IO Exception.", ex));
                }
            }
            updateRelatedProducts(product, product.getFranchises(), FOLDER_NAME_FRANCHISES);

            if (product.getPicture() != null) {
                Picture picture = product.getPicture();
                try {
                    if (NetUtils.downloadFromUrl(StringUtils.isEmpty(picture.getUrlFull())
                            ? picture.getUrlSmall()
                            : picture.getUrlFull(),
                            productDir.toAbsolutePath().toString()
                            + FileUtils.getFileSystem().getSeparator() + "product.jpg")) {
                        picture.setPictureLocaleStorage(true);
                        picture.setPictureExtension("jpg");
                        picture.setPictureLocation("product.jpg");
                    }
                } catch (IOException ex) {
                    result.getSubOperations().add(new FileOperationResult(
                            true, "Couldn't download artist.jpg IO Exception.", ex));
                }

            }

            FukoDB.getInstance().getProductService().updateFileSystem(product);
        }

        return result;
    }

    public static FileOperationResult updateRelatedProducts(Product product, List<ProductLink> products, String folderName) {
        FileOperationResult result = new FileOperationResult();

        Path productDir;
        try {
            productDir = getProductDirectory(product, true);
        } catch (IOException ex) {
            result.setSuccess(false);
            result.setMessage("Coudn't get ArtistDir -  IOExpcetion " + ex.getMessage());
            result.setException(ex);
            return result;
        }

        for (ProductLink link : products) {
            try {
                Path albumDir = getProductDirectory(link, true);
                Files.createSymbolicLink(RyouPath.create(productDir.toAbsolutePath().toString(),
                        folderName,
                        createFolderName(link)).getPath(),
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

    public static FileOperationResult updateRelatedAlbums(Product product, List<AlbumLink> albums, String folderName) {
        FileOperationResult result = new FileOperationResult();

        Path artistDir;
        try {
            artistDir = getProductDirectory(product, true);
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
