package com.example.imagecomparator.model;


import lombok.Data;

import java.util.List;

@Data
public class ConfigYaml {

    /** 元画像配置ディレクトリ */
    private String srcDir;

    /** 比較対象のディレクトリスト */
    private List<DirectoryInfoToCompare> directoryInfoToCompare;

}
