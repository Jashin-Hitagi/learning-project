package com.example.demo.binlog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.FileOutputStream;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SqlTable {

    private long tableId;

    /**
     * 库名
     */
    private String database;

    /**
     * 表名
     */
    private String table;

    /**
     * 写出的文件流
     */
    private FileOutputStream fos;

}
