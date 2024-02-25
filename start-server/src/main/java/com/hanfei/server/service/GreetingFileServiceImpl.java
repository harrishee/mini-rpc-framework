package com.hanfei.server.service;

import com.hanfei.api.GreetingFileService;
import com.hanfei.rpc.anno.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Service
public class GreetingFileServiceImpl implements GreetingFileService {
    @Override
    public String createGreetingFileToServer(String name, String address, String fileName) {
        String content = "Hello! this is: [" + name + "], my address is: [" + address + "]\n";
        
        try (FileWriter fileWriter = new FileWriter(new File(fileName), true)) {
            fileWriter.write(content);
            return "Operation done: " + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return "Error in creating file";
        }
    }
}
