package de.knoobie.project.ryou.filesystem.domain;

import de.knoobie.project.clannadutils.bo.ClannadOperationResult;


public class FileOperationResult extends ClannadOperationResult<RyouPath> {

    public FileOperationResult() {
    }

    public FileOperationResult(boolean success) {
        super(success);
    }

    public FileOperationResult(boolean success, String message) {
        super(success, message);
    }

    public FileOperationResult(boolean success, String message, Throwable exception) {
        super(success, message, exception);
    }

    public FileOperationResult(boolean success, String message, Throwable exception, RyouPath result) {
        super(success, message, exception, result);
    }

}
