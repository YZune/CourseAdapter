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

/**
 * @author realzns
 * @date 2022/05/03
 */
public class JXNUParser extends Parser {
    String source;
    Document doc;

    public JXNUParser(@NotNull String source) {
        super();
        this.source = source;
        doc = Jsoup.parse(source);
    }

    @NotNull
    @Override
    public List<Course> generateCourseList() {
        List<Course> courses = new ArrayList<>();
        Elements trs = doc.getElementById("_ctl1_NewKcb").select("tr");
        for (int row = 1; row < trs.size(); row++) {
            Elements tds = trs.get(row).select("td");
            for (Element td : tds) {
                if (td.attr("bgcolor").equals("#66FFCC")) {
                    int day = tds.indexOf(td);
                    int startNode = getStartNode(row);
                    int duration = getDuration(startNode);

                    if (row == 1 || row == 6) {
                        //表格这两行多了一列
                        day--;
                    }

                    System.out.printf("第%d天 第%d节: ", day, getStartNode(row));
                    Course c = getCourse(td);
                    System.out.println("|" + startNode + "|" + (startNode + duration));
                    c.setStartNode(startNode);
                    c.setEndNode(startNode + duration - 1);
                    courses.add(c);
                    //System.out.println(c);
                }
            }
        }
        System.out.println("Done.");


        return courses;
    }

    private Course getCourse(Element td) {
        /**
         * 本适配方式依赖于HTML中课程信息的如下格式
         *
         * <div align="center">
         *  课程名
         *  <br>( xxxx教室 )
         *  <br>班级信息 &nbsp;
         * </div>
         *
         * 若有更改可联系 realzns[at]jxnu.edu.cn 请求适配
         */
        //System.out.println(td.html());
        String name, room, teacher;
        Pattern p = Pattern.compile(">\\s*(.*)\\s*<br>\\((.*)\\)\\s*<br>(.*)\\s*</div>");
        Matcher m = p.matcher(td.html());
        m.find();
        name = m.group(1);
        room = m.group(2).trim();
        teacher = m.group(3).replace(" &nbsp;", "");
        System.out.print(name + "|" + room + "|" + teacher);

        //本方法中只填入 name、room、teacher
        //startWeek、endWeek、credit 在此 html 中获取不到
        return new Course(name, 0, room, teacher,
                0, 0, 1, 20, 0, 0f, "", "", "", );
    }

    private int getStartNode(int row) {
        return switch (row) {
            case 1, 6 -> row;
            case 2, 3, 4, 7 -> row + 1;

            //晚课
            case 8 -> 10;
            default -> -1;
        };
    }

    private int getDuration(int startNode) {
        return switch (startNode) {
            case 1, 6, 8, 10 -> 2;
            default -> 1;
        };
    }
}
