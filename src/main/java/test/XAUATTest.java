package main.java.test;

import main.java.parser.XAUATParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class XAUATTest {
    public static void main(String[] args) throws IOException {
        XAUATParser xauatParser = new XAUATParser(
                Files.readString(Path.of("C:\\Users\\akhzz\\Desktop\\研究生教育综合管理系统(学生服务).htm")));
        xauatParser.saveCourse(true);
    }
}
