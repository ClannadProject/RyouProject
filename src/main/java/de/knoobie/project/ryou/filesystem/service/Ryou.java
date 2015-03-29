package de.knoobie.project.ryou.filesystem.service;

import de.knoobie.project.clannadutils.common.NetUtils;
import de.knoobie.project.ryou.filesystem.domain.FileOperationResult;
import de.knoobie.project.ryou.filesystem.utils.InitializeClannadFileSystem;
import java.io.IOException;

public class Ryou {

    public static void main(String[] args) throws IOException {
        
        if(NetUtils.downloadFromUrl("http://vgmdb.net/db/assets/covers/38626-1370611541.png", 
                "Z:\\Clannad\\38626-1370611541.png")){
            System.out.println("Download successful");
        }else{
            System.out.println("Nope.");
        }
    }

    private static void initClannadFileStructur() {
        FileOperationResult result = InitializeClannadFileSystem.initClannadFileSystem("Z:\\Clannad");
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
