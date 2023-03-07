package com.iktpreobuka.projekat.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.projekat.security.Views;

@Controller
@RequestMapping(path = "/api/project")
public class FileDownloadController {

	@JsonView(Views.Admin.class) //TODO samo administrator može da vidi/preuzme logove
	protected final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
	
	//SECURED ADMIN
	//Zadatak treba da omogući da administrator i samo administrator može da
	//vidi/preuzme logove.
	@RequestMapping(method = RequestMethod.GET, value = "/download")
    public ResponseEntity<ByteArrayResource> downloadFile() throws IOException {

        File file = new File("logs/spring-boot-logging.log");
        Path path = Paths.get(file.getAbsolutePath());

        if (!file.exists()) {
            logger.error("File doesn't exist");
            return ResponseEntity.notFound().build();
        }

        logger.info("File is ready for download.");
        
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);    
        }

}
