package main.java.parser;

import bean.Course;
import org.jetbrains.annotations.NotNull;
import parser.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 西安建筑科技大学研究生教务
 * 系统登录地址：https://gmis.xauat.edu.cn/pyxx
 * 在 教学与培养 -> 课表查询 导入
 * 若有适配不完善，可在本人fork的项目下提issue
 *
 * @author akhzz
 * @date 2023/9/8
 */
public class XAUATParser extends Parser {

    private final String jsInitFunc;
    // CT:草堂校区，雁塔校区信息暂缺
    private final Pattern CT_LONG_Pattern = Pattern.compile("草堂校区-草堂校区\\d{1,2}号楼-草堂\\d{1,2}-\\d{1,3}");
    private final Pattern CT_SHORT_Pattern = Pattern.compile("草堂校区-草堂\\d{1,2}号楼-\\d{1,2}-\\d{1,3}");

    public XAUATParser(@NotNull String source) {
        super(source);
        jsInitFunc = source.substring(source.indexOf("function init(){"), source.indexOf("};"));
    }

    @NotNull
    @Override
    public List<Course> generateCourseList() {
        String dayRegex = "td_\\d_\\d{2}";
        List<Course> courseList = new ArrayList<>();
        int startIndex;
        int endIndex;
        Pattern pattern = Pattern.compile(dayRegex);
        Matcher matcher = pattern.matcher(jsInitFunc);
        while (matcher.find()) {
            // 提取当前条课程所在星期及节数
            String dayStr = matcher.group().substring(3);
            // 提取课程信息
            startIndex = matcher.start();
            startIndex = jsInitFunc.indexOf("+=\"课程", startIndex);
            endIndex = jsInitFunc.indexOf("\";", startIndex);
            String courseInfo = jsInitFunc.substring(startIndex + 3, endIndex);
            courseList.add(parseItem(dayStr, courseInfo));
        }

        // 暴力合并相邻小节课程，辣鸡系统小节是单独的一条信息，我比较蔡，如果有更优雅的方法可以替换一下
        // 在合并时，周范围一直的信息才会合并，周范围不一致的小节将不会合并（课表里应该没有这样的）
        for (int i = courseList.size() - 1; i >= 0; i--) {
            Course iCourse = courseList.get(i);
            for (int j = 0; j < i; j++) {
                Course jCourse = courseList.get(j);
                // 检查是不是同一门课
                if (!iCourse.getName().equals(jCourse.getName())) {
                    continue;
                }
                // 检查是不是同一天，同样周范围的课
                if (iCourse.getStartWeek() != jCourse.getStartWeek()
                        || iCourse.getEndWeek() != jCourse.getEndWeek()
                        || iCourse.getDay() != jCourse.getDay()) {
                    continue;
                }
                if (iCourse.getEndNode() + 1 == jCourse.getStartNode()) {
                    jCourse.setStartNode(iCourse.getStartNode());
                }
                if (jCourse.getEndNode() + 1 == iCourse.getStartNode()) {
                    jCourse.setEndNode(iCourse.getEndNode());
                }
                courseList.remove(i);
                break;
            }
        }
        return courseList;
    }

    private Course parseItem(String dayStr, String courseInfo) {
        String name;      // 课程名
        int day;       // 该课程的是星期几（7代表星期天）参数范围：1 - 7
        String room;      // 教室
        String teacher;   // 老师
        int startNode;    // 开始为第几节课
        int endNode;      // 结束时为第几节课
        int startWeek;    // 开始周
        int endWeek;      // 结束周
        int type = 0;         // 单双周，每周为0，单周为1，双周为2

        // 节数和星期是表格的id中包含的，例如：td_2_44，其中2代表周二，44代表第4节课（减去40）
        String[] weekAndNode = dayStr.split("_");
        day = Integer.parseInt(weekAndNode[0]);
        startNode = Integer.parseInt(weekAndNode[1]) - 40;
        endNode = startNode;
        String[] info = courseInfo.split("<br>");
        name = info[0].substring(3);
        teacher = info[2].substring(5);
        // 周数和地点在一起，后面用括号标注的周数，例如：
        // 教室:草堂校区-草堂14号楼-14-101(第3-5周 连续周 )
        room = info[3].substring(info[3].indexOf(":") + 1, info[3].indexOf("("));
        room = simplyRoom(room);
        String[] weekInfo = info[3].substring(info[3].indexOf("(") + 1, info[3].length() - 2).split(" ");
        if (weekInfo[0].contains("-")) {
            startWeek = Integer.parseInt(weekInfo[0].substring(1, weekInfo[0].indexOf("-")));
            endWeek = Integer.parseInt(weekInfo[0].substring(weekInfo[0].indexOf("-") + 1, weekInfo[0].length() - 1));
        } else {
            // 2023/9/8 暂时没有仅一个周的数据，猜测一个周显示的样式为“第n周”
            startWeek = Integer.parseInt(weekInfo[0].substring(1, weekInfo[0].length() - 1));
            endWeek = startWeek;
        }

        // 2023/9/8 暂时没有单双周的数据，猜测显示的是“单周”和“双周”
        if ("单周".equals(weekInfo[1])) {
            type = 1;
        } else if ("双周".equals(weekInfo[1])) {
            type = 2;
        }

        return new Course(name, day, room, teacher, startNode, endNode, startWeek, endWeek, type, 0.0f,
                "", "", "");
    }

    // 简化上课地址字符串，防止过长在app显示时遮挡
    private String simplyRoom(String rawStr) {
        Matcher ctLongMatcher = CT_LONG_Pattern.matcher(rawStr);
        Matcher ctShortMatcher = CT_SHORT_Pattern.matcher(rawStr);
        if (ctLongMatcher.find()) {
            return rawStr.replaceAll("草堂校区-草堂校区\\d{1,2}号楼-", "");
        }
        if (ctShortMatcher.find()) {
            return rawStr.replaceAll("号楼-\\d{1,2}-", "-").substring(5);
        }
        return rawStr;
    }
}
