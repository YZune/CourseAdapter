package main.java.test;

import main.java.parser.HNZJParser;
import main.java.parser.JXNUParser;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class JXNUTest {
    public static void main(String[] args) throws IOException {
        FileReader fileReader = new FileReader("/Users/realzns/Downloads/test.html", StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        int len;
        char[] chars = new char[1024];
        while ((len = fileReader.read(chars)) != -1) {
            stringBuilder.append(chars, 0, len);
        }
        JXNUParser jxnuParser = new JXNUParser(stringBuilder.toString());
        jxnuParser.generateCourseList();
    }
}
