package main.java.parser;

import bean.Course;
import main.java.bean.TimeDetail;
import main.java.bean.TimeTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import parser.CSVParser;
import parser.Parser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//This class depends on the CSVParser class! The csv generation code is basically translated from https://mp.weixin.qq.com/s/7w9TG3nZbu85pRQ5SeGZEA .

//This implement does not depend on jsoup, and it employs regular expression to parse the "我的课程表（本+研）" page in the
//微人大 system. This approach is not robust if the page's structure or its curriculum datum structure is changed. In
//some extreme cases, even the texts of course names, teacher names, etc., could affect the result. Therefore, 你必须手工检查
//结果。

//!!!Note: This class depends on the CSVParser class, so if the standard grammar of csv curricula is changed, or the
//CSVParser class is no longer available, this class should be updated too.

//Some information that is not available, such as credits, is all left default. A careful manual check is always
//necessary.

//<>本解析器只适用于微人大的“我的课程表（本+研）”页面，请不要在选课系统页面上使用！<>
//<>注意！！第十三节到第十四节课的上课时间在“我的课程表（本+研）”页面和教务系统有所不同；务必自行了解；这好像也是这学期刚改的；反正尽量早点去吧。<>
//<>本解析器使用的方法不能保证结果的正确性！请务必手动检查！请务必自行设置起始周和学期长度等信息！<>
//<>一些在页面上不能获取到的信息，例如学分，都保留了初始值。再次提醒，请手动检查结果。<>

//<>本解析器依赖于CSV课表解析器。如果CSV解析器更新导致本解析器失效，请联系我。<>

public class RUCParser extends Parser {

    // There will be some warnings about redundant character escape, but to fix this we will have to
    // use some grammar which some other regular expression engines do not support. So, I did not modify
    // these regular expressions.
    private static final Pattern course_list_re = Pattern.compile("\"course\": \\[([\\s\\S]*?)\\]");
    private static final Pattern each_course_re = Pattern.compile("\\{[\\s\\S]*?\\}");
    private static final Pattern each_attribute_re = Pattern.compile("\"(\\S+)\": {1,2}\"([\\S\\s]*?)\"");
    private static final Pattern hours_list_re = Pattern.compile("\"schoolHours\": \\[([\\s\\S]*?)\\]");
    private static final Pattern each_hour_re = Pattern.compile("\"([0-9]{1,2}):([0-9]{1,2})~([0-9]{1,2}):([0-9]{1,2})\"");
    String source;

    public RUCParser(@NotNull java.lang.String source) {
        super(source);
        this.source = source; // I have only learned very little about kotlin and java, but it seems that `super(source)` should have given us a `final String source` and `source = this.source`?
        //But the line above is necessary, and I do not know why.
    }

    private static String join(List<String> list, String delimiter) {
        StringBuilder builder = new StringBuilder();
        for (String i : list) {
            builder.append(i);
            builder.append(delimiter);
        }
        return builder.substring(0, builder.length() - delimiter.length());
    }

    private static String format_a_line(List<String> items) {
        // return ",".join(items)
        return RUCParser.join(items, ",");
    }


    private static String generate_csv(List<List<String>> rows) {
        List<String> title = Arrays.asList("课程名称", "星期", "开始节数", "结束节数", "老师", "地点", "周数");



        return RUCParser.join(Stream.concat(Stream.of(title), rows.stream()).map(RUCParser::format_a_line).collect(Collectors.toList()), "\n");
    }

    private static List<Map<String, String>> document_parse(String document) {
        ArrayList<Map<String, String>> attr_dicts = new ArrayList<>();
        Matcher course_list_match = course_list_re.matcher(document);
        if (course_list_match.find()) {
            String course_list_text = course_list_match.group(0);
            Matcher courses = each_course_re.matcher(course_list_text);
            while (courses.find()) {
                Map<String, String> attr_dict = new TreeMap<>();
                String strc = courses.group(0);
                Matcher attributes = each_attribute_re.matcher(strc);
                while (attributes.find()) {
                    attr_dict.put(attributes.group(1), filter(attributes.group(2)));
                }
                transform_q(attr_dict);
                attr_dicts.add(attr_dict);
            }
        }
        return attr_dicts;
    }

    private static void transform_q(Map<String, String> attr_dict) {
        attr_dict.put("end", Integer.toString(Integer.parseInt(attr_dict.get("start")) + Integer.parseInt(attr_dict.get("quittingTime")) - 1));
        attr_dict.put("weekSpan", week_parse(attr_dict.get("weekly")));
    }

    private static String week_parse(String weekly) {
        Matcher week_match = Pattern.compile("第([0-9]{1,2}-[0-9]{1,2})周([单全双])周").matcher(weekly);
        if (week_match.find()) {
            String span = week_match.group(1);
            String odd_even = week_match.group(2);
            if (odd_even.equals("全")) {
                return span;
            } else {
                return span.concat(odd_even);
            }
        } else
            throw new PatternSyntaxException("week format is illegal", "第([0-9]{1,2}-[0-9]{1,2})周([单全双])周", -1);
    }

    private static String filter(String attr) {
        if (attr.contains(",")) {
            attr = attr.replaceAll(",", "、");
        }
        if (attr.contains(":")) {
            return attr.split(": ")[1];
        } else if (attr.contains("：")) {
            return attr.split("：")[1];
        } else if (attr.contains("\\(")) {
            return attr.split("：")[0];
        } else if (attr.equals("")) {
            return "无";
        } else return attr;
    }

    private static <A, B> List<B> map_get(Map<A, B> dict, List<A> list) {
        return list.stream().map(dict::get).collect(Collectors.toList());
    }

    private static List<List<String>> give_course_attr_lists(List<Map<String, String>> attr_dicts) {
        return attr_dicts.stream().map(x -> map_get(x, Arrays.asList("title", "week", "start", "end", "teacher", "place", "weekSpan"))).collect(Collectors.toList());
    }


    @NotNull
    @Override
    public List<Course> generateCourseList() {
        return new CSVParser(generate_csv(give_course_attr_lists(document_parse(source)))).generateCourseList();
    }

    @NotNull
    @Override
    public String getTableName() {
        return "人大导入";
    }

    private static String add_zero(String str) {
        if (str.length() == 1) {
            return "0".concat(str);
        } else return str;
    }

    @Nullable
    @Override
    public TimeTable generateTimeTable() {
        ArrayList<TimeDetail> times = new ArrayList<>();
        Matcher hour_match = hours_list_re.matcher(this.source);
        if (hour_match.find()) {
            Matcher each_match = each_hour_re.matcher(hour_match.group(1));
            int count = 1;
            while (each_match.find()) {
                times.add(
                        new TimeDetail(count,
                                String.format("%s:%s",
                                        add_zero(each_match.group(1)),
                                        add_zero(each_match.group(2))),
                                String.format("%s:%s",
                                        add_zero(each_match.group(3)),
                                        add_zero(each_match.group(4))))
                );
                count++;
            }
        }
        return new TimeTable("人大作息", times);
    }
}
