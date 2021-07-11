package com.example.imagecomparator.model;

import j2html.tags.DomContent;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static j2html.TagCreator.*;

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
        List<DirectoryInfo> comparingDirectoryInfos = configYaml.getDirectoryInfoToCompare().stream()
                .map(rawDirInfo -> {
                    DirectoryInfo dirInfo = new DirectoryInfo();
                    dirInfo.setTitle(rawDirInfo.getTitle());
                    dirInfo.setFileList(convertToPathList(rawDirInfo.getDir()));
                    return dirInfo;
                }).collect(Collectors.toList());

        String report = renderReport(srcFileList, comparingDirectoryInfos);
        outputReport(report);
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

    private String renderReport(List<Path> srcFileList, List<DirectoryInfo> comparingDirectoryInfos){
        List<String> labels = Arrays.asList("0", "1");
        Stream<DomContent> labelList = comparingDirectoryInfos.stream().flatMap(info -> Stream.of(th("0"), th("1")));
        return table(
                tbody(
                        tr(
                                th("元ファイルパス"),
                                th("元画像"),
                                each(comparingDirectoryInfos, i -> th(attrs(".headerLabel"),i.getTitle()).withColspan("2"))
                        ),
                        tr(
                                th(),
                                th(),
                                each(labelList)
                        )
                )
        ).render();
    }

    private void outputReport(String report) {

        try (PrintWriter w = new PrintWriter(new File("./report.html"), StandardCharsets.UTF_8)) {
            w.print(report);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Data
    private static class DirectoryInfo{
        private String title;
        private List<Path> fileList;
    }


}
