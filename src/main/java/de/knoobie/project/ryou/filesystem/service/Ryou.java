package de.knoobie.project.ryou.filesystem.service;

import de.knoobie.project.fuko.database.domain.Artist;
import de.knoobie.project.fuko.database.domain.Organization;
import de.knoobie.project.fuko.database.domain.Product;
import de.knoobie.project.fuko.database.service.FukoDB;
import de.knoobie.project.nagisa.gson.model.dto.json.organisation.Organisation;
import de.knoobie.project.ryou.filesystem.domain.FileOperationResult;
import de.knoobie.project.ryou.filesystem.domain.RyouPath;
import de.knoobie.project.ryou.filesystem.utils.ArtistFileSystem;
import de.knoobie.project.ryou.filesystem.utils.InitializeClannadFileSystem;
import de.knoobie.project.ryou.filesystem.utils.OrganizationFileSystem;
import de.knoobie.project.ryou.filesystem.utils.ProductFileSystem;
import java.io.IOException;
import java.nio.file.Files;

public class Ryou {

    public static void main(String[] args) throws IOException {

//        if(NetUtils.downloadFromUrl("http://vgmdb.net/db/assets/covers/38626-1370611541.png", 
//                "Z:\\Clannad\\38626-1370611541.png")){
//            System.out.println("Download successful");
//        }else{
//            System.out.println("Nope.");
//        }
//        initClannadFileStructur();
//        RyouPath folder = RyouPath.create("I:\\ClannadProject\\Album", "Decennia AKINO with bless4 [50563]");
//        if (FileUtils.exists(folder.getPath())) {
//            System.out.println("Exists - do nothing");
//        } else {
//            System.out.println("Don't exists - will be added");
//            FileUtils.createDirectory(folder.getPath());
//        }
//        Album d = FukoDB.getInstance().getAlbumService().findByVGMdbID(50564);
//        
//        AlbumFileSystem.createCompleteAlbumStructure(d);
//          if(AlbumFileSystem.hasLocalFolder(a)){
//              System.out.println("!ist vorhanden!");
//          }else{
//              AlbumFileSystem.createDirectory(a);
//          }
//        RyouPath folder2 = RyouPath.create("I:\\ClannadProject\\Album\\456");
//        if (FileUtils.exists(folder2.getPath())) {
//            System.out.println("Exists - do nothing");
//        } else {
//            System.out.println("Don't exists - will be added");
//            FileUtils.createDirectory(folder2.getPath());
//        }
        // FileAlreadyExistsException
//        
//        Artist a = FukoDB.getInstance().getArtistService().findByVGMdbID(6);
//        ArtistFileSystem.createCompleteArtistStructure(a, true);
        for (int i = 1; i <= 5; i++) {
            Organization a = FukoDB.getInstance().getOrganizationService().findByVGMdbID(i);
            OrganizationFileSystem.createCompleteOrganizationStructure(a, true);
        }

    }

    private static void dddd() {
        
//          for (int i = 1018; i <= 1023; i++) {
//            Product a = FukoDB.getInstance().getProductService().findByVGMdbID(i);
//            ProductFileSystem.createCompleteProductStructure(a, true);
//        }
        
        
        //        Album a = FukoDB.getInstance().getAlbumService().findByVGMdbID(50563);        
//        AlbumFileSystem.createCompleteAlbumStructure(a, false);
//        
//        Album b = FukoDB.getInstance().getAlbumService().findByVGMdbID(50564);        
//        AlbumFileSystem.createCompleteAlbumStructure(b, false);
//        
//        Album c = FukoDB.getInstance().getAlbumService().findByVGMdbID(49046);        
//        AlbumFileSystem.createCompleteAlbumStructure(c, false);
    }

    private static void initClannadFileStructur() {
        FileOperationResult result = InitializeClannadFileSystem.initClannadFileSystem("I:\\ClannadProject");
        if (result.isSuccess()) {
            System.out.println("============ SUCCESS =============");
            System.out.println(result.getMessage());
        } else {
            System.out.println("============ FAILED ==============");
            System.out.println(result.getMessage());
            System.out.println("SubMessage: ");
            System.out.println("Submessages: " + result.getSubOperations().size());
            result.getSubOperations().stream().forEach((subResult) -> {
                System.out.println(">>> " + subResult.getMessage());
            });
        }
    }

}
