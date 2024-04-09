package et.kaleb.sftpclient.util;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import et.kaleb.sftpclient.service.EmailHandlerService;
import et.kaleb.sftpclient.service.FileTransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;


@Component
public class Scheduler {
    @Autowired
    private FileTransferService fileTransferService;

    @Autowired
    private EmailHandlerService emailHandlerService;

    private Logger logger = LoggerFactory.getLogger(Scheduler.class);

    @Scheduled(cron = "0/10 * * * * ?")//every hour
    public void authorizedReport() throws JSchException, SftpException {

        logger.info("Connecting to sFTP server to download files");

        ArrayList<File> files = fileTransferService.downloadSFTPFile();
        logger.info("Successfully downloaded files");
        for (File file : files) {
            emailHandlerService.send(file);
        }



    }

    @Scheduled(cron = "0 0/120 * * * ?")//every two hours
    public void consolidatedReport() throws JSchException, SftpException {

        logger.info("Consolidated report");


    }
}
