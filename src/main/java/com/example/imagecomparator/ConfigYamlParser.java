package com.example.imagecomparator;

import com.example.imagecomparator.model.ConfigYaml;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@Component
public class ConfigYamlParser {

    public void loadYaml(String path) throws FileNotFoundException {

        try(InputStream inputStream = new FileInputStream(new File(path))){
            Yaml yaml = new Yaml(new Constructor(ConfigYaml.class));
            ConfigYaml configYaml = yaml.load(inputStream);
            System.out.println(configYaml);

        }catch(Exception e){
            throw new FileNotFoundException("failed to find specified file >>" + path);
        }

    }
}
