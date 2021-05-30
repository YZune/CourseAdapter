package main.java.parser;

import bean.Course;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import parser.Parser;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * 适配河南职业技术学院教务系统KINGOSOFT
 * 在 主页-->网上选课-->选课结果 导入
 *
 * @author chanvstone
 * @version 1.0
 * @date 2021-05-30
 */

public class HNZJParser extends Parser {
    public HNZJParser(@NotNull String source) {
        super(source);
    }

    @NotNull
    @Override
    public List<Course> generateCourseList() {
        ArrayList<Course> courses = new ArrayList<>();
        Document document = Jsoup.parse(getSource());
        Elements trs = document.selectFirst("table#reportArea").selectFirst("tbody").select("tr");//全部行
        for (Element tr : trs) {
            List<Course> courseList = createCourse(tr);
            courses.addAll(courseList);
        }

        System.out.println(courses.size());

        return courses;
    }

    /**
     * @param tr 表格的每一行
     * @return 根据这一行创建的课程列表
     */
    private List<Course> createCourse(Element tr) {
        ArrayList<Course> courses = new ArrayList<>();
        Elements tds = tr.select("td");
        /*
        列    内容
        1     课程名
        2     学分
        4     讲师名
        12    上课时间和地点
         */
        String name = tds.get(1).text().replaceAll("\\[[\\w\\s]*\\]", "");
        float credit = Float.parseFloat(tds.get(2).text());
        String teacher = tds.get(4).text();
        String room;
        int startWeek;
        int endWeek;
        int type = 0;
        int day;
        int startNode;
        int endNode;

        for (TextNode textNode : tds.get(12).selectFirst("font").textNodes()) {

            //分离上课时间和教室
            String date;//上课时间
            String[] date_room = textNode.text().split("\u2002");
            if (date_room.length == 2) {
                date = date_room[0];
                room = date_room[1];
            } else {
                //没有指定教室
                room = "";
                date = textNode.text().substring(0, textNode.text().length() - 1);
            }


            //分离周数、星期数、课序数
            int index_weekday = date.indexOf("周");
            String weeks = date.substring(0, index_weekday);//周数
            String weekday = date.substring(index_weekday + 1, index_weekday + 2);//星期
            String nodes = date.substring(index_weekday + 2);//课序

            //设置day
            switch (weekday) {
                case "一":
                    day = 1;
                    break;
                case "二":
                    day = 2;
                    break;
                case "三":
                    day = 3;
                    break;
                case "四":
                    day = 4;
                    break;
                case "五":
                    day = 5;
                    break;
                case "六":
                    day = 6;
                    break;
                case "日":
                    day = 7;
                    break;
                default:
                    day = 1;
            }

            //设置startNode, endNode
            String[] startNode_endNode = nodes.split("-");
            startNode = Integer.parseInt(startNode_endNode[0]);
            endNode = Integer.parseInt(startNode_endNode[1]);

            //weeks
            weeks = weeks.substring(1, weeks.length() - 1);
            if (weeks.contains(",")) {
                for (String weekRange : weeks.split(",")) {
                    if (weekRange.contains("-")) {
                        String[] startWeek_endWeek = weekRange.split("-");
                        startWeek = Integer.parseInt(startWeek_endWeek[0]);
                        endWeek = Integer.parseInt(startWeek_endWeek[1]);
                    } else {
                        startWeek = endWeek = Integer.parseInt(weekRange);
                    }
                    courses.add(new Course(name, day, room, teacher, startNode, endNode, startWeek, endWeek, type, credit, "", "", ""));
                }

            } else {
                if (weeks.contains("-")) {
                    String[] startWeek_endWeek = weeks.split("-");
                    startWeek = Integer.parseInt(startWeek_endWeek[0]);
                    endWeek = Integer.parseInt(startWeek_endWeek[1]);
                } else {
                    startWeek = endWeek = Integer.parseInt(weeks);
                }
                courses.add(new Course(name, day, room, teacher, startNode, endNode, startWeek, endWeek, type, credit, "", "", ""));
            }

        }

        return courses;
    }

}
