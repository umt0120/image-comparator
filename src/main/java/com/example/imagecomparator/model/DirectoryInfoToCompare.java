package com.example.imagecomparator.model;

import lombok.Data;

@Data
public class DirectoryInfoToCompare {
    /** 表示用タイトル */
    private String title;

    /** 画像配置用ディレクトリ */
    private String dir;
}
