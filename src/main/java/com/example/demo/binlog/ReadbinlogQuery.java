package com.example.demo.binlog;

import com.github.shyiko.mysql.binlog.BinaryLogFileReader;
import com.github.shyiko.mysql.binlog.event.*;
import com.github.shyiko.mysql.binlog.event.deserialization.ChecksumType;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * @author wh
 * 通过binlog 进行 sql数据恢复
 */
public class ReadbinlogQuery {

    private static final Pattern PATTERN = Pattern.compile("^[-\\+]?[\\d]*$");

    public static void main(String[] args) throws IOException {
        Scanner input = new Scanner(System.in);
        String avg = "1";
        while (!"q".equals(avg)){
            parseLog();
            System.out.println("继续恢复按回车，退出输入q：");
            avg = input.nextLine();
        }
        System.out.println("已退出");
    }

    public static void parseLog() throws IOException{
        Scanner input = new Scanner(System.in);
        System.out.println("请输入要恢复的binlog文件所在文件夹：");
        String fileList = input.nextLine() + File.separator;
        System.out.println("请输入要恢复的binlog文件名：");
        String binlog = input.nextLine();
        String filePath = fileList + binlog;
        String outFilePath = fileList + binlog.substring(binlog.lastIndexOf(".") + 1) + File.separator;
        File binlogFile = new File(filePath);
        File outFile = new File(outFilePath);
        if (!outFile.exists()) {
            outFile.mkdirs();
        }
        EventDeserializer eventDeserializer = new EventDeserializer();
        eventDeserializer.setChecksumType(ChecksumType.CRC32);
        try (BinaryLogFileReader reader = new BinaryLogFileReader(binlogFile,
                eventDeserializer)) {
            // 准备写入的文件名称
            Map<Long, SqlTable> tableMap = new HashMap<>(16);
            //所有内容
            FileOutputStream fos = createFos(outFilePath + binlog);
            //query sql 语句
            FileOutputStream queryFos = createFos(outFilePath + binlog + ".sql");
            for (Event event; (event = reader.readEvent()) != null; ) {
                if (EventType.TABLE_MAP.equals(event.getHeader().getEventType())) {
                    TableMapEventData data = event.getData();
                    if (tableMap.containsKey(data.getTableId())) {
                        continue;
                    }
                    tableMap.put(data.getTableId(),
                            SqlTable.builder()
                                    .tableId(data.getTableId())
                                    .database(data.getDatabase())
                                    .table(data.getTable())
                                    .fos(createFos(outFilePath + data.getDatabase() + "." + data.getTable() + ".csv"))
                                    .build());
                }
                if (EventType.QUERY.equals(event.getHeader().getEventType())) {
                    QueryEventData data = event.getData();
                    queryFos.write((data.getSql() + ";" + "\n").getBytes());
                }
                if (EventType.EXT_WRITE_ROWS.equals(event.getHeader().getEventType())) {
                    WriteRowsEventData data = event.getData();
                    if (tableMap.containsKey(data.getTableId())) {
                        for (Serializable[] serializables : data.getRows()) {
                            queryFos.write((arrayToInsertSql(serializables, tableMap.get(data.getTableId()).getTable()) + ";" + "\n").getBytes());
                        }
                    }
                }
                if (EventType.EXT_DELETE_ROWS.equals(event.getHeader().getEventType())) {
                    DeleteRowsEventData data = event.getData();
                    if (tableMap.containsKey(data.getTableId())) {
                        for (Serializable[] serializables : data.getRows()) {
                            tableMap.get(data.getTableId()).getFos().write((arrayToString(serializables) + "\n").getBytes());
                        }
                    }
                }
                // 把数据写入到输出流
                fos.write((event.toString() + "\n").getBytes());
            }
            // 关闭输出流
            fos.close();
            queryFos.close();
            tableMap.entrySet().forEach((entry -> {
                try {
                    entry.getValue().getFos().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
            System.out.println("日志恢复完成");
        }
    }

    public static String arrayToString(Object[] a) {
        if (a == null)
            return "null";

        int iMax = a.length - 1;
        if (iMax == -1)
            return "";

        StringBuilder b = new StringBuilder();
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.toString();
            b.append(", ");
        }
    }

    public static String arrayToInsertSql(Object[] a, String tableName) {
        if (a == null)
            return "null";

        int iMax = a.length - 1;
        if (iMax == -1)
            return "";

        StringBuilder b = new StringBuilder();
        b.append("INSERT INTO `").append(tableName).append("` values (");
        for (int i = 0; ; i++) {
            b.append("'").append(a[i]).append("'");
            if (i == iMax)
                return b.append(" )").toString();
            b.append(", ");
        }
    }

    @SneakyThrows
    public static  FileOutputStream createFos(String filePath){
        deleteFile(filePath);
        return  new FileOutputStream(filePath, true);
    }

    public static void deleteFile(String filePath){
        File outFile = new File(filePath);
        if (outFile.exists()) {
            outFile.delete();
        }
    }

}
