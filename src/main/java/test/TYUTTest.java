package main.java.test;

import bean.Course;
import main.java.parser.TYUTParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class TYUTTest {
    public static void main(String[] args) {
        String source = readFileToString("C:\\Users\\25698\\Desktop\\CourseAdapter\\src\\main\\java\\parser\\Course.html");
        assert source != null;
        TYUTParser parser = new TYUTParser(source);
        List<Course> list = parser.generateCourseList();
        for (Course course : list) {
            System.out.println(course.getName());
        }
    }

    /**
     * 读取文本文件的内容并返回一个字符串。
     *
     * @param filePath 文件路径
     * @return 文件内容作为字符串，如果读取失败则返回null
     */
    public static String readFileToString(String filePath) {
        StringBuilder content = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            return null;
        }

        // 移除最后一个换行符（如果存在）
        if (!content.isEmpty() && content.charAt(content.length() - 1) == '\n') {
            content.setLength(content.length() - 1);
        }

        return content.toString();
    }


}
