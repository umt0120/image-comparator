package com.example.imagecomparator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class ImageComparatorRunner implements ApplicationRunner {

    @Autowired
    ConfigYamlParser configYamlParser;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> configs = args.getOptionValues("config");
        if(Objects.nonNull(configs) & configs.size() == 1) {
            configYamlParser.loadYaml(configs.get(0));
        }else{
            System.out.println("Specify the relative path of your yaml file for configuration!");
        }
    }

}
