package et.kaleb.sftpclient.service;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;


public interface EmailHandlerService {

    boolean send(File files) throws JSchException, SftpException;
}