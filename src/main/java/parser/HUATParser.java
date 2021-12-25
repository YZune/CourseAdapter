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
                int current_day = 0; // 范围:0-6 指代星期一到星期天

                for (Element td_node : td_list) {
                    if (!td_node.hasAttr("align")){
                        if (td_node.children().size() != 0 && td_node.child(0).is("table")) {

                            Elements tables = td_node.select("table");
                            Element info;
                            for (Element table : tables) {
                                info = table.selectFirst("tr").selectFirst("td");
                                addCourse(Objects.requireNonNull(info).text(), node_level_index, current_day, courses);
                            }
                        }
                        current_day++;
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

        int start_node, end_node, start_week, end_week;

        if (node_level != 4) {
            end_node = 2 * (node_level + 1);
            start_node = end_node - 1;
        } else {
            end_node = 2 * (node_level + 1) + 1;
            start_node = end_node - 2;
        }

        String[] elements = infos.split("\\s+");

        String[] week;

        if (elements.length > 3) {
            week = removeChineseCharacter(elements[elements.length - 1]).split("-");
        } else {
            week = removeChineseCharacter(elements[2]).split("-");
        }



        start_week = Integer.parseInt(week[0]);
        end_week = Integer.parseInt(week[1]);

        courses.add(
                new Course(
                        elements[0], current_day, elements[3],
                        elements[1], start_node, end_node,
                        start_week, end_week, 0, 1f, "", "", ""));

    }


}
