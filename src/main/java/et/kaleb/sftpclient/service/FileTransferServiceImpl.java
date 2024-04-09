package et.kaleb.sftpclient.service;

import com.jcraft.jsch.*;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;

@Service
public class FileTransferServiceImpl implements FileTransferService {

    private Logger logger = LoggerFactory.getLogger(FileTransferServiceImpl.class);

    @Value("${sftp.host}")
    private String host;

    @Value("${sftp.port}")
    private Integer port;

    @Value("${sftp.username}")
    private String username;

    @Value("${sftp.password}")
    private String password;

    @Value("${sftp.path}")
    private String path;

    @Value("${sftp.file.prefix}")
    private String prefix;

    @Value("${sftp.file.postfix}")
    private String postfix;//

    @Value("${sftp.sessionTimeout}")
    private Integer sessionTimeout;

    @Value("${sftp.channelTimeout}")
    private Integer channelTimeout;

    @Value("${sftp.known.host}")
    private String knownHosts;//

    @Override
    public boolean uploadFile(FTPFile fTPFile) {

        return false;
    }

    @Override
    public boolean deleteFile(String fileName) {
        FTPClient client = new FTPClient();
        FTPFile[] response = new FTPFile[0];
        try {
            client.connect(host);
            client.login(username, password);

            if (client.isConnected()) {
                client.deleteFile(path + "/" + fileName);
                return true;
            }
            client.logout();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return false;
    }

    @Override
    public boolean renameFile(String fileName, String toFileName) {
        FTPClient client = new FTPClient();
        FTPFile[] response = new FTPFile[0];
        try {
            client.connect(host);
            client.login(username, password);

            if (client.isConnected()) {
                client.rename(path + "/" + fileName, path + "/" + toFileName);
                return true;
            }
            client.logout();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return true;
    }

    @Override
    public boolean renameFTPFile(String fileName, String toFileName) throws JSchException, SftpException {

        JSch jsch = new JSch();
        jsch.setKnownHosts(knownHosts);
        Session jschSession = jsch.getSession(username, host, port);

        jschSession.setPassword(password);

        Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        jschSession.setConfig(config);

        jschSession.connect();

        Channel sftp = jschSession.openChannel("sftp");

        sftp.connect(sessionTimeout);

        ChannelSftp channelSftp = (ChannelSftp) sftp;

        channelSftp.rename(path + "/" + fileName, path + "/" + toFileName);

        channelSftp.exit();
        return false;
    }

    @Override
    public ArrayList<File> downloadFile() {
        FTPClient client = new FTPClient();
        FTPFile[] fTPFiles = new FTPFile[0];
        ArrayList<File> files = new ArrayList<File>();
        try {
            client.connect(host);
            client.login(username, password);

            if (client.isConnected()) {
                // Obtain a list of filenames in the current working
                // directory. When no file found an empty array will
                // be returned.
                FTPFileFilter ftpFileFilter = new FTPFileFilter() {
                    @Override
                    public boolean accept(FTPFile ftpFile) {
                        return ftpFile.getName().toLowerCase().startsWith(prefix.toLowerCase());
                    }
                };
                fTPFiles = client.listFiles(path, ftpFileFilter);

                for (FTPFile ftpFile : fTPFiles) {
                    InputStream iStream = client.retrieveFileStream(ftpFile.getName());
                    File localFile = new File(ftpFile.getName());
                    FileOutputStream fout = new FileOutputStream(localFile);

                    boolean success = client.retrieveFile(path + "/" + ftpFile.getName(), fout);

                    if (success) {
                        files.add(localFile);
                        fout.flush();
                        fout.close();
                    } else {
                        logger.info("Retrieve failure");
                    }
                }

                return files;
            }
            client.logout();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return files;
    }

    @Override
    public ArrayList<File> downloadSFTPFile() throws JSchException, SftpException {
        ArrayList<File> files = new ArrayList<File>();
        JSch jsch = new JSch();
        jsch.setKnownHosts(knownHosts);
        Session jschSession = jsch.getSession(username, host, port);

        jschSession.setPassword(password);

        Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        jschSession.setConfig(config);

        jschSession.connect();

        Channel sftp = jschSession.openChannel("sftp");

        sftp.connect(sessionTimeout);

        ChannelSftp channelSftp = (ChannelSftp) sftp;
        Vector filelist = channelSftp.ls(path);

        ///add filter here

        for (int i = 0; i < filelist.size(); i++) {
            ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) filelist.get(i);

            if (entry.getFilename().toLowerCase().startsWith(prefix.toLowerCase()) && entry.getFilename().toLowerCase().endsWith(postfix.toLowerCase())) {

                channelSftp.get(path + "/" + entry.getFilename(), entry.getFilename());
                File file = new File(entry.getFilename());
                files.add(file);
                logger.info("downloaded file: " + entry.getFilename());
            }
        }

        channelSftp.exit();
        return files;
    }


}