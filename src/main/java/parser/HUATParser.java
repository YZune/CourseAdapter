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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 *  本适配严重依赖于HTML中课程信息的固有格式
 *  即 "大学生心理健康（4）杜XX 3H 6208 2-10周（全周）"
 *  如果出现更改 请联系2182998627@qq.com请求适配
 *
 *  2021-12-25 00:48:20 **NekoRectifier**
 */


public class HUATParser extends Parser {
    
    String source;
    Document document;

    public HUATParser(@NotNull String source) {
        super(source);
        this.source = source;

        document = Jsoup.parse(source);
    }

    @NotNull
    @Override
    public List<Course> generateCourseList() {
        ArrayList<Course> courses = new ArrayList<>();

        Element base_table = document.getElementById("ctl00_ContentPlaceHolder1_CourseTable");

        if (Objects.nonNull(base_table)) {
            Elements horizontal_list = base_table.select("[valign=\"middle\"]");

            int node_level_index = 0;
            //由于HUAT的课表排列是横向在HTML中给出的,故采用该方式来获得具体课程的节数. 0-4 分别指定1-2,3-4,....节课
            for (Element horizontal : horizontal_list) {

                Elements td_list = horizontal.select("td");
                int current_day = 1; // 范围:1-7 指代星期一到星期天

                for (Element td_node : td_list) {
                    if (!td_node.hasAttr("align")){  // 去掉表格元素
                        if (td_node.children().size() != 0 && td_node.child(0).is("table")) {
                            Elements tables = td_node.select("table");
                            Element info;
                            for (Element table : tables) {
                                info = table.selectFirst("tr").selectFirst("td");
                                addCourse(Objects.requireNonNull(info).text(), node_level_index, current_day, courses);
                            }
                            current_day++;
                        } else if (td_node.children().size() == 0) {
                            current_day++;
                        }
                    } //有align的都是表格描述之类的
                }
                node_level_index++;
            }
        }

        return courses;
    }

    public String removeChineseCharacter(String str) {
        Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher matcher = pattern.matcher(str);
        return matcher.replaceAll("").replaceAll("（）","");
    }

    void addCourse(String infos,int node_level, int current_day, ArrayList<Course> courses) {
        String[] elements = infos.split("\\s+");
        String[] week = removeChineseCharacter(elements[elements.length - 1]).split("-");  //无论什么情况周数都在elements的最后
        int start_node, end_node, start_week, end_week;
        String class_name = elements[0], class_room, teacher;

        start_week = Integer.parseInt(week[0]);
        end_week = Integer.parseInt(week[1]);

        if (node_level != 4) {                                  //node_level = 4 时是指晚上3节课的情况
            end_node = 2 * (node_level + 1);
            start_node = end_node - 1;
        } else {
            end_node = 2 * (node_level + 1) + 1;
            start_node = end_node - 2;
        }

        switch (elements.length) {
            case 5: // 正常完整课表情况 名称+教师+2/3H+教室+周数
                class_room = elements[3];
                teacher = elements[1];
                break;
            case 4:                     // 缺少教师/教室信息
                if (elements[elements.length - 2].contains("H")) { //判断元素数组中倒数第二个是否是"2H或3H,以确定elements中缺少的元素"
                    // 无教室情况
                    class_room = "未知教室";
                    teacher = elements[1];
                } else {
                    // 无教师情况
                    teacher = "未知教师";
                    class_room = elements[2];
                }
                break;
            default:
                int length = elements.length;
                if (length > 5) { // 多教师情况
                    class_room = elements[length - 2];
                    int[] name_range = {1,length - 1 - 1 - 1 - 1};
                    StringBuilder teacherBuilder = new StringBuilder();
                    for (int i = name_range[0]; i <= name_range[1]; i++) {
                        teacherBuilder.append(" ").append(elements[i]);
                    }
                    teacher = String.valueOf(teacherBuilder);
                } else { // 多少有点问题 \ 缺少教室和教师信息
                    teacher = "未知教师";
                    class_room = "未知教室";
                }
                
                break;
        }

        courses.add(
                new Course(
                        class_name, current_day, class_room,
                        teacher, start_node, end_node,
                        start_week, end_week, 0, 1f, "", "", ""));
    }


}
