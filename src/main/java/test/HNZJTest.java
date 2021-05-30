package main.java.test;

import main.java.parser.HNZJParser;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;

public class HNZJTest {
    public static void main(String[] args) throws IOException {
        FileReader fileReader = new FileReader("./HNZJ.html", Charset.forName("GBK"));
        StringBuilder stringBuilder = new StringBuilder();
        int len;
        char[] chars = new char[1024];
        while ((len = fileReader.read(chars)) != -1) {
            stringBuilder.append(chars, 0, len);
        }
        HNZJParser hnzjParser = new HNZJParser(stringBuilder.toString());
        hnzjParser.generateCourseList();
    }
}
