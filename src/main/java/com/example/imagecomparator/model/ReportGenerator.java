package com.example.imagecomparator.model;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ReportGenerator {

    /**
     * 設定値を記載したYAMLファイルをもとにHTML形式のレポートを出力する
     * @param configYaml 設定値を記載したYAMLファイル
     */
    public void generateReport(ConfigYaml configYaml){

        // 元ファイル一覧
        List<Path> srcFileList = convertToPathList(configYaml.getSrcDir());

        // 比較対象ディレクトリ一覧
        List<DirectoryInfo> directoryInfos = configYaml.getDirectoryInfoToCompare().stream()
                .map(rawDirInfo -> {
                    DirectoryInfo dirInfo = new DirectoryInfo();
                    dirInfo.setTitle(rawDirInfo.getTitle());
                    dirInfo.setFileList(convertToPathList(rawDirInfo.getDir()));
                    return dirInfo;
                }).collect(Collectors.toList());

        System.out.println(srcFileList);
        directoryInfos.stream().forEach(System.out::println);

    }

    private List<Path> convertToPathList(String path){
        List<Path> ret = new ArrayList<>();
        try {
            ret = Files.walk(Path.of(path))
                    .filter(eachFile -> !Files.isDirectory(eachFile))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Data
    private static class DirectoryInfo{
        private String title;
        private List<Path> fileList;
    }


}
