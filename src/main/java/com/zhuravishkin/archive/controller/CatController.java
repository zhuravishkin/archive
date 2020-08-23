package com.zhuravishkin.archive.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.*;
import com.zhuravishkin.archive.configuration.ActuatorConfig;
import com.zhuravishkin.archive.model.Cat;
import com.zhuravishkin.archive.service.CatService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "message")
public class CatController {
    private final CatService service;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper mapper;
    private final ActuatorConfig actuatorConfig;

    private static final int PORT = 22;

    @Autowired
    public CatController(CatService service, RabbitTemplate rabbitTemplate, ObjectMapper mapper,
                         ActuatorConfig actuatorConfig) {
        this.service = service;
        this.rabbitTemplate = rabbitTemplate;
        this.mapper = mapper;
        this.actuatorConfig = actuatorConfig;
    }

    @GetMapping(value = "/get")
    public ResponseEntity<List<Cat>> getCats() {
        log.info("Get method start...");
        List<Cat> cats = service.findAll();
        List<String> jsonList = new ArrayList<>();
        log.info("Objects from tables springboottable:");
        for (Cat c : cats) {
            log.info(c.toString());
            try {
                jsonList.add(mapper.writeValueAsString(c));
            } catch (JsonProcessingException e) {
                log.error(e.getMessage(), e);
            }
        }
        log.info("Objects from tables springboottable in JSON:");
        for (String s : jsonList) {
            log.info(s);
        }
        return new ResponseEntity<>(cats, HttpStatus.OK);
    }

    @PostMapping(value = "/post")
    @ResponseBody
    public ResponseEntity<List<Cat>> postCats(
            @RequestParam String exchange,
            @RequestParam String key,
            @RequestBody Cat cat) {
        log.info("Post method start...");
        log.info("Objects Cat:");
        log.info(cat.toString());
        log.info("Upload objects:");
        List<Cat> cats = service.findCats(cat.getName(), cat.getAge(), cat.getDateTime());
        for (Cat c : cats) {
            log.info(c.toString());
        }
        log.info("Upload objects completed successfully");
        log.info("Sending message...");
        try {
            rabbitTemplate.convertAndSend(exchange, key, mapper.writeValueAsString(cats));
            actuatorConfig.getSendMessage().increment();
            actuatorConfig.getReceivedMessage().increment();
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            actuatorConfig.getLostMessage().increment();
        }
        log.info("Message is sent successfully");
        File file = new File(UUID.randomUUID() + ".csv");
        try (FileWriter fileWriter = new FileWriter(file)) {
            writeCat(cats, fileWriter);
            putToSftpServer(file);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return new ResponseEntity<>(cats, HttpStatus.OK);
    }

    private void writeCat(List<Cat> cats, FileWriter fileWriter) {
        log.info("Write to csv-file...");
        try (CSVPrinter printer = new CSVPrinter(fileWriter, CSVFormat.DEFAULT)) {
            for (Cat cat : cats) {
                printer.printRecords(
                        cat.getId() + "," + cat.getName() + "," + cat.getAge() + "," + cat.getDateTime());
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        log.info("File is recorded");
    }

    private void putToSftpServer(File file) {
        log.info("Uploading file to sftp-server");
        try {
            JSch jSch = new JSch();
            jSch.setKnownHosts(new ByteArrayInputStream(("192.168.0.13 ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQDiwOLh" +
                    "lVnECztwxZp9ptShLq8t+9+FZSb6S5edEhQu5vQUCv1y+qk1OrjvZOY1LKpHJkH8Gx7HLBgP80LMYnjFNBrbgJt71CJ1pR" +
                    "eiYlpNuZ8VRBqdBEZTtY4ghDoAaIzrovLGqK0Uo3N4vfctA7BAcNw96yWPHz9pWBhjEKGvL3ALSizuUkv6XGIAULxcCiUe" +
                    "jywB427wldiL14WDMhzp4jUORXtY7V2kRsWWPuU51uNvafmFt1d3SepNUTS5SRd70y3OgAy4l0CCl80+gEkMjzFJxSQZhe" +
                    "us/W6NFue/MuyMCCciJOZoyrH2NoOngB52h90ZJ8+tb1gol8LrY+ZodHTd81sJ309kkDK6RT3odflTi7SGh2gZbWzHWQ+1" +
                    "040ex0nTzlqBfHeyhLjyyX0nSDaibHh6WcPCNpHHF+vdXSSTNxkOa6OtNIUfRBmO5Z1j5FX+7DChzE0i7rTyZxKZmt+ZQV" +
                    "AUbFz9YirX3bUu98E80kEPXDdoIg7aS7b+4SM=").getBytes()));
            jSch.addIdentity("id_rsa", "ubuntu");
            Session session = jSch.getSession("user", "192.168.0.13", PORT);
            session.connect();
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp channelSftp = (ChannelSftp) channel;
            String path = ".sftp";
            String[] sftpPath = path.split("/");
            StringBuilder currentDirectory = new StringBuilder(channelSftp.pwd());
            for (String dir : sftpPath) {
                SftpATTRS sftpATTRS = null;
                try {
                    sftpATTRS = channelSftp.stat(currentDirectory.append("/").append(dir).toString());
                } catch (SftpException e) {
                    log.info("Directory not found: " + currentDirectory);
                }
                if (sftpATTRS != null) {
                    log.info("Directory \"" + currentDirectory + "\" exists: " + sftpATTRS.isDir());
                } else {
                    log.info("Creating directory: " + currentDirectory);
                    channelSftp.mkdir(currentDirectory.toString());
                }
            }
            channelSftp.cd(path);
            channelSftp.put(file.getAbsolutePath(), UUID.randomUUID() + ".csv");
            channelSftp.exit();
            channel.disconnect();
            session.disconnect();
            log.info("The file has been uploaded to the sftp-server successfully");
        } catch (JSchException | SftpException e) {
            log.error(e.getMessage(), e);
        }
    }
}
