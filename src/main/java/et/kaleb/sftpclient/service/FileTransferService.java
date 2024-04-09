package et.kaleb.sftpclient.service;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.util.ArrayList;

public interface FileTransferService {

    boolean uploadFile(FTPFile file);

    boolean deleteFile(String fileName);

    boolean renameFile(String fileName, String toFileName);

    boolean renameFTPFile(String fileName, String toFileName) throws JSchException, SftpException;

    ArrayList<File> downloadFile();
    ArrayList<File> downloadSFTPFile() throws JSchException, SftpException;
}
