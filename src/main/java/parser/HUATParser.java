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
import java.util.Objects;

/*
 *  本适配依赖于HTML中课程信息的固有格式
 *  即 "大学生心理健康（4）杜XX 3H 6208 2 - 10周（全周）"
 *  如果出现更改 请联系 2182998627@qq.com
 *
 *  2023年2月1日星期三 CST 下午4:16:57 **NekoRectifier**
 */

public class HUATParser extends Parser {

    Document document;

    public HUATParser(@NotNull String source) {
        super();
        document = Jsoup.parse(source);
    }

    @NotNull
    @Override
    public List<Course> generateCourseList() {
        Element base_table = document.getElementById("ctl00_ContentPlaceHolder1_CourseTable");

        byte row_num = 0;
        byte col_num = 1;

        ArrayList<Course> courses = new ArrayList<>();
        if (Objects.nonNull(base_table)) {
            Elements horizontal_list = base_table.select("[valign=\"middle\"]");

            //由于 HUAT 的课表排列是横向在HTML中给出的,故采用该方式来获得具体课程的节数. 0-4 分别指定1-2,3-4,....节课
            for (Element horizontal : horizontal_list) {
//                System.out.println(horizontal);
                Elements td_list = horizontal.select("td>a:first-of-type");

                for (Element td_node : td_list) {
                    System.out.println(td_node.parent().text());
                    addCourse(td_node.parent().text(), row_num, col_num, courses);
                    col_num++;
                }
//                System.out.println(row_num);
                row_num++;
                col_num = 1;
            }
        }
        return courses;
    }

    void addCourse(String info, int period_index, int day_index, ArrayList<Course> courses) {
        int start_node, end_node, start_week, end_week, week_start_index, week_end_index;

        String[] elements = info.split(" ");
        String class_name = elements[0], class_room, teacher;
        week_start_index = elements.length - 3;
        week_end_index = elements.length - 1;

        start_week = Integer.parseInt(elements[week_start_index]);
        end_week = Integer.parseInt(
                (elements[week_end_index]
                        .replaceAll("[周（）全半]", "")));

        //period_index = 4 时是指晚上3节课的情况
        if (period_index != 4) {
            end_node = 2 * (period_index + 1);
            start_node = end_node - 1;
        } else {
            end_node = 2 * (period_index + 1) + 1;
            start_node = end_node - 2;
        }

        int length = elements.length;
        switch (length) {

            case 7 -> {
                // 正常完整课表情况 {名称 + 教师 + 2/3H + 教室 + start 周数+ - + end周数}
                class_room = elements[3];
                teacher = elements[1];
            }

            case 6 -> {
                // 缺少教师/教室信息
                if (elements[length - 4].contains("H")) {
                    // 无教室情况
                    class_room = "未知教室";
                    teacher = elements[1];
                } else {
                    // 无教师情况
                    teacher = "未知教师";
                    class_room = elements[2];
                }
            }

            case 5 -> { //  缺少教室和教师信息
                teacher = "未知教师";
                class_room = "未知教室";
            }

            default -> {
                if (length > 7) {
                    StringBuilder builder = new StringBuilder();

                    // 多教师情况
                    // 通常来讲，多教师的课程是会有已知教室号的
                    if (elements[length - 4].replaceAll("[0-9]{4}", "").equals("")) {
                        class_room = elements[length - 4];

                        for(int i = 1; i< length - 5; i++) {
                            builder.append(elements[i]).append(" ");
                        }
                        teacher = builder.toString().trim();
                    } else { //课程是无教室号的
                        class_room = "未知教室";

                        for(int i = 1; i< length - 4; i++) {
                            builder.append(elements[i]).append(" ");
                        }
                        teacher = builder.toString().trim();
                    }
                } else {
                    teacher = "未知教师";
                    class_room = "未知教室";
                }
            }
        }

        courses.add(
                new Course(
                        class_name, day_index, class_room,
                        teacher, start_node, end_node,
                        start_week, end_week, 0, 1f, "", "", "", ));
    }
}
