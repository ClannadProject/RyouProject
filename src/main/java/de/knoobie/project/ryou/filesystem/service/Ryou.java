package de.knoobie.project.ryou.filesystem.service;

import de.knoobie.project.ryou.filesystem.domain.FileOperationResult;
import de.knoobie.project.ryou.filesystem.utils.InitializeClannadFileSystem;
import java.io.IOException;

public class Ryou {

    public static void main(String[] args) throws IOException {
//        RyouPath watchPath = RyouPath.create("Z:\\Musik.ClannadProject");
//        watchPath.do2();

        FileOperationResult result = InitializeClannadFileSystem.initClannadFileSystem("Z:\\Clannad");

        if (result.isSuccess()) {
            System.out.println("============ SUCCESS =============");
            System.out.println(result.getMessage());
        } else {
            System.out.println("============ FAILED ==============");
            System.out.println(result.getMessage());
            System.out.println("SubMessage: ");
            System.out.println("Submessages: "+result.getSubOperations().size());
            result.getSubOperations().stream().forEach((subResult) -> {
                    System.out.println(">>> "+subResult.getMessage());
//                    subResult.getException().printStackTrace();                
            });
        }
//        System.out.println("isSuccess " + result.isSuccess());
//        System.out.println("Message" + result.getMessage());
//        result.getException().printStackTrace();
    }

}
