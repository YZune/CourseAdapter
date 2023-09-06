package main.java.parser;

import bean.Course;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import parser.Parser;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//广东第二师范学院
public class GDEIParser extends Parser {

    Document document;
    public GDEIParser(@NotNull String source) {
        super(source);
        document = Jsoup.parse(source);
    }

    @NotNull
    private List<Integer> TimeParser(String NodeString){ //获取Node 和 Week
        List<Integer> Node = new ArrayList<>();

        // 使用正则表达式模式匹配数字
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(NodeString);

        while (matcher.find()) {
            String numberStr = matcher.group();
            int number = Integer.parseInt(numberStr);
            Node.add(number);
        }

        return Node;
    }
    private String RoomParser(@NotNull String roomString){
        String room = roomString;
        int index = roomString.lastIndexOf('(');
        if (index != -1){
            room = roomString.substring(0,index).trim();
        }
        return room;
    }

    @NotNull
    @Override
    public List<Course> generateCourseList() {
        List<Course> courseArrayList = new ArrayList<>();
        Element element = document.selectFirst("tbody");//找到显示课程表
        Elements elements = element.select("tr");

        for (Element tableElement : elements) {
            Elements courses = tableElement.select("td");

            for (int i = 1; i < courses.size(); i++) { //i为星期
                //System.out.println("星期"+i);
                Elements elementInfos = courses.get(i).select("div div");
                if (elementInfos.isEmpty()) continue;
                //System.out.println(elementInfos.text());
                List<Integer> time = TimeParser(elementInfos.get(1).text());
                //eg:线性代数 1-12 周 (第1,2节) 22软件工程C (30 人) 花教610 (HDJX004900) 王森洪
                courseArrayList.add(new Course(elementInfos.get(0).text(), i, RoomParser(elementInfos.get(3).text()),
                        elementInfos.get(4).text(), time.get(2), time.get(3), time.get(0),
                        time.get(1), 0, 0f, elementInfos.get(2).text(), "", ""));

                if (elementInfos.size()>5){
                    time = TimeParser(elementInfos.get(6).text());
                    courseArrayList.add(new Course(elementInfos.get(5).text(), i, RoomParser(elementInfos.get(8).text()),
                            elementInfos.get(9).text(), time.get(2), time.get(3), time.get(0),
                            time.get(1), 0, 0f, elementInfos.get(7).text(), "", ""));
                }
            }
        }
        return courseArrayList;
    }
}