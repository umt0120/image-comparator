package com.example.imagecomparator.model;

import j2html.tags.DomContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static j2html.TagCreator.*;

@Component
public class ReportGenerator {

    private static final String label0Suffix = "_2_0.jpg";
    private static final String label1Suffix = "_2_1.jpg";
    private static final String extension = ".jpg";

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
                    dirInfo.setImgFileNamePairList(convertToFileNameListToImgFileNamePairList(rawDirInfo.getDir()));
                    // dirInfo.setFileList(convertToPathList(rawDirInfo.getDir()));
                    // dirInfo.setFileNamesCorrespondingToTheOrigin(convertToFileNameListCorrespondingToTheOrigin(rawDirInfo.getDir()));
                    return dirInfo;
                }).collect(Collectors.toList());

        List<ReportRawInfo> reportRawInfoList = createRawInfo(configYaml, srcFileList, comparingDirectoryInfos);

        String report = renderReport(comparingDirectoryInfos,reportRawInfoList);
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

    private List<String> convertToFileNameListCorrespondingToTheOrigin(String path){
        List<String> ret = new ArrayList<>();

        try {
            ret = Files.walk(Path.of(path))
                    .filter(eachFile -> !Files.isDirectory(eachFile))
                    .map(eachFile -> {
                        // prefix除去（YAMLに書いある./srcとか）
                        String prefixRemoved = eachFile.toString().replace(path, "");
                        if(prefixRemoved.contains(label0Suffix)){
                            // gradcam出力画像に付与されている_2_0とかを取り除いて返す
                           return prefixRemoved.replace(label0Suffix,extension);
                        }else{
                            return prefixRemoved.replace(label1Suffix,extension);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }
    private List<ImgFileNamePair> convertToFileNameListToImgFileNamePairList(String path){
        List<ImgFileNamePair> ret = new ArrayList<>();

        try {
            ret = Files.walk(Path.of(path))
                    .filter(eachFile -> !Files.isDirectory(eachFile))
                    .map(eachFile -> {
                        // prefix除去（YAMLに書いある./srcとか）
                        String prefixRemoved = eachFile.toString().replace(Paths.get(path).toString(), "");
                        if(prefixRemoved.contains(label0Suffix)){
                            // gradcam出力画像に付与されている_2_0とかを取り除いて返す
                            return new ImgFileNamePair(eachFile,prefixRemoved.replace(label0Suffix,extension));
                        }else{
                            return new ImgFileNamePair(eachFile,prefixRemoved.replace(label1Suffix,extension));
                        }
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private String renderReport(List<DirectoryInfo> comparingDirectoryInfos, List<ReportRawInfo> reportRawInfoList){

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
                        ),
                        each(reportRawInfoList, raw -> tr(
                                th(raw.getSrcImgPath()),
                                th(img().withSrc(raw.getSrcImgPath()).withWidth("255").withHeight("255")),
                                each(raw.getImgPathListToBeCompared(), col -> th(img().withSrc(col.toString()).withWidth("255").withHeight("255")))
                        ))
                )
        ).render();
    }

    private List<ReportRawInfo> createRawInfo(ConfigYaml configYaml, List<Path> srcFileList, List<DirectoryInfo> comparingDirectoryInfos){

        List<ReportRawInfo> reportRawInfoList = new ArrayList<>();

        srcFileList.stream().forEach(srcFile -> {

            ReportRawInfo reportRawInfo = new ReportRawInfo();
            reportRawInfo.setSrcImgPath(srcFile.toString());
            List<Path> imgPathListToBeCompared = new ArrayList<>();
            // 症例以降だけを切り出した元画像ファイルパス
            String originalFilePathAfterTheCase = srcFile.toString().replace(Paths.get(configYaml.getSrcDir()).toString(), "");

            comparingDirectoryInfos.stream()
                    .forEach(directoryInfo -> {
                       List<ImgFileNamePair> pairList =  directoryInfo.getImgFileNamePairList().stream()
                               .filter(eachFile -> Objects.equals(originalFilePathAfterTheCase.replace(".jpg", "").replace(".bmp",""), eachFile.getFileNameCorrespondingToTheOrigin().replace(".jpg", "").replace(".bmp","")))
                               .collect(Collectors.toList());
                       if(pairList.size() == 2){
                           pairList.stream().forEach(eachFile -> imgPathListToBeCompared.add(eachFile.getRealFilePath()));
                       }else{
                           imgPathListToBeCompared.add(Paths.get(""));
                           imgPathListToBeCompared.add(Paths.get(""));
                       }
                    });
            reportRawInfo.setImgPathListToBeCompared(imgPathListToBeCompared);
            reportRawInfoList.add(reportRawInfo);
        });
        return reportRawInfoList;
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
        private List<ImgFileNamePair> imgFileNamePairList;
    }

    @AllArgsConstructor
    @Data
    private static class ImgFileNamePair{
       private Path realFilePath;
       private String fileNameCorrespondingToTheOrigin;
    }

    @Data
    private static class ReportRawInfo{
        /** 比較元画像ファイルパス*/
        private String srcImgPath;
        /** 比較対象画像ファイルパスリスト（GradCAM画像（ラベル0）,GradCAM画像（ラベル1）, GradCAM++画像（ラベル0）, GradCAM++画像（ラベル1） ....）*/
        private List<Path> imgPathListToBeCompared;
    }

}
