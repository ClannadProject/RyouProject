package de.knoobie.project.ryou.filesystem.utils;

import de.knoobie.project.clannadutils.common.FileUtils;
import de.knoobie.project.clannadutils.common.StringUtils;
import de.knoobie.project.ryou.filesystem.domain.FileOperationResult;
import de.knoobie.project.ryou.filesystem.domain.RyouPath;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class InitializeClannadFileSystem {

    private static final String[] ClannadDirectoryNames = new String[]{"Artist", "Album", "Product", ".new"};

    public static FileOperationResult initClannadFileSystem(String clannadBase) {
        FileOperationResult result = new FileOperationResult();

        if (StringUtils.isEmpty(clannadBase)) {
            result.setSuccess(false);
            result.setMessage("No Clannad Base selected. Please select a valid directory.");
            return result;
        }

        RyouPath folder = RyouPath.create(clannadBase);

        if (folder == null) {
            result.setSuccess(false);
            result.setMessage("Selected Clannad Base doesn't exist. Please select a valid directory.");
            return result;
        }

        if (!FileUtils.isDirectory(folder.getPath())) {
            result.setSuccess(false);
            result.setMessage("Selected Clannad Base is not a directory. Please select a valid directory.");
            return result;
        }

        List<Path> subDirs = new ArrayList<>();
        for (String subDirName : ClannadDirectoryNames) {
            try {
                subDirs.add(FileUtils.createSubDirectory(folder.getPath(), subDirName));
            } catch (FileAlreadyExistsException ex) {
                result.getSubOperations().add(new FileOperationResult(true, "Couldn't create directory '" + subDirName + "' already exists.", ex));
            } catch (IOException ex) {
                result.getSubOperations().add(new FileOperationResult(false, "Couldn't create directory '" + subDirName + "'.", ex));
                result.setSuccess(false);
                result.setMessage("Couldn't create subfolder '" + subDirName + "' in 'Clannad Base'.");
                result.setException(ex);
                return result;
            } catch (IllegalArgumentException ex) {
                result.getSubOperations().add(new FileOperationResult(false, ex.getMessage(), ex));
                result.setSuccess(false);
                result.setMessage("Couldn't create subfolder '" + subDirName + "' in 'Clannad Base'. " + ex.getMessage());
                result.setException(ex);
                return result;
            }
        }

        if (subDirs.size() == ClannadDirectoryNames.length) {
            result.setSuccess(true);
            result.setMessage("Clannad filestructur successful created.");
        } else {
            result.setSuccess(false);
            result.setMessage("Clannad filestructur not fully applied.");
        }
        return result;
    }
}
