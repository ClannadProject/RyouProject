package de.knoobie.project.ryou.filesystem.domain;

import de.knoobie.project.clannadutils.common.FileUtils;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;


public class RyouPath {

    private final Path path;

    private RyouPath(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }
    public static RyouPath create(String startPath, String... folder) throws InvalidPathException {
        return new RyouPath(FileUtils.getFileSystem().getPath(startPath, folder));
    }

    public static RyouPath create(String path) throws InvalidPathException {
        return new RyouPath(FileUtils.getFileSystem().getPath(path));
    }

    public void do2() throws IOException {
        WatchService watchService = path.getFileSystem().newWatchService();
        WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
        while (true) {
            watchKey.pollEvents().stream().map((event)
                    -> (Path) event.context()).forEach((newPath) -> {
                        System.out.println("New file: " + newPath.toAbsolutePath().toString());
                    });
        }
    }
}
