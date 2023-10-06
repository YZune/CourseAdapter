package main.java.parser;

import bean.Course;
import main.java.bean.TimeDetail;
import main.java.bean.TimeTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import parser.Parser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 西安建筑科技大学研究生教务
 * 系统登录地址：https://gmis.xauat.edu.cn/pyxx（注：该系统要求IE10+或Chrome，需要调整UA，否则有弹窗无法登陆）
 * 在 教学与培养 -> 课表查询 导入
 * 若有适配不完善，可在本人fork的项目下提issue
 *
 * @author akhzz
 * @date 2023/9/8
 */
public class XAUATParser extends Parser {

    private final String jsInitFunc;

    private int maxWeek;
    private List<Course> courseList;
    private TimeTable timeTable;
    private int maxNode;
    private String tableName;
    private String startDate;

    public XAUATParser(@NotNull String source) {
        super(source);
        jsInitFunc = source.substring(source.indexOf("function init(){"), source.indexOf("};"));
    }

    @Nullable
    @Override
    public String getStartDate() {
        final HashMap<String, String> START_DATE = new HashMap<>();
        START_DATE.put("2022-2023-1", "2022-8-28");
        START_DATE.put("2022-2023-2", "2023-2-12");
        START_DATE.put("2023-2024-1", "2023-8-27");
        START_DATE.put("2023-2024-2", "2024-2-25");
        START_DATE.put("2024-2025-1", "2024-8-25");
        if (startDate != null) {
            return startDate;
        }
        if (tableName == null) {
            tableName = getTableName();
        }
        // 优先使用手动录入的结果
        // 来源：教务处，https://jwc.xauat.edu.cn/lszq/xljzxsj.htm
        if (START_DATE.containsKey(tableName)) {
            startDate = START_DATE.get(tableName);
            return startDate;
        }
        // 如果没有手动录入的数据就使用教务处网页的数据，这个数据只有当前学期的（开学后过几天才会更新）
        // 来源：教务处网页右上角的周数对应后台的input，https://jwc.xauat.edu.cn
        Document doc;
        try {
            doc = Jsoup.connect("https://jwc.xauat.edu.cn/").get();
        } catch (IOException e) {
            return null;
        }
        Elements elements = doc.select(".top_one > div:nth-child(1) > div:nth-child(1) > input:nth-child(6)");
        String startDateFromJwc = elements.first().attr("value");
        if ("".equals(startDate)) {
            return null;
        }
        // 2023-2024-1学期的设置的日期是2023-8-28，这一天是周一，但实际上学校使用周日作为一周的第一天，这里对数据做了修正
        startDate = moveDay(startDateFromJwc, "yyyy-M-dd", -1);
        return startDate;
    }

    @Nullable
    @Override
    public Integer getNodes() {
        if (maxNode == 0) {
            timeTable = generateTimeTable();
        }
        return maxNode;
    }

    @Nullable
    @Override
    public String getTableName() {
        if (tableName != null) {
            return tableName;
        }
        Document doc = Jsoup.parse(getSource());
        Elements elements = doc.select("#drpxq > option[selected=\"selected\"]");
        return elements.first().text();
    }

    @Nullable
    @Override
    public Integer getMaxWeek() {
        if (maxWeek == 0) {
            courseList = generateCourseList();
        }
        // 程序里面的maxWeek是按最晚结束课程算的，
        // 统计了一下校历，一般教学周最短19周左右，如果太短就用19周代替，够19就加3防止后面记录考试啥的
        return maxWeek >= 19 ? maxWeek + 3 : 19;
    }

    @NotNull
    @Override
    public List<Course> generateCourseList() {
        if (courseList != null) {
            return courseList;
        }
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
                // 检查是不是同一门课，各信息是否一致
                if (!iCourse.getName().equals(jCourse.getName())
                        || iCourse.getStartWeek() != jCourse.getStartWeek()
                        || iCourse.getEndWeek() != jCourse.getEndWeek()
                        || iCourse.getType() != jCourse.getType()
                        || iCourse.getDay() != jCourse.getDay()
                        || !iCourse.getTeacher().equals(jCourse.getTeacher())
                        || !iCourse.getRoom().equals(jCourse.getRoom())) {
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

    // 获取每一条课程的信息
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
        // 周数和地点在一起，后面用括号标注的周数，部分课程没有安排教室
        // 教室:草堂校区-草堂14号楼-14-101(第3-5周 连续周 )
        if (info[3].substring(info[3].indexOf(":") + 1).startsWith("(")) {
            room = "";
        } else {
            room = info[3].substring(info[3].indexOf(":") + 1, info[3].indexOf("("));
            room = simplyRoom(room);
        }
        String[] weekInfo = info[3].substring(info[3].indexOf("(") + 1, info[3].length() - 2).split(" ");
        startWeek = Integer.parseInt(weekInfo[0].substring(1, weekInfo[0].indexOf("-")));
        endWeek = Integer.parseInt(weekInfo[0].substring(weekInfo[0].indexOf("-") + 1, weekInfo[0].length() - 1));

        if (endWeek > maxWeek) {
            maxWeek = endWeek;
        }
        // 没有发现单双周的数据，猜测显示的是“单周”和“双周”，暂时先留着
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
        // 草堂校区-草堂校区13号楼-草堂13-306
        if (Pattern.compile("草堂校区-草堂校区\\d{1,2}号楼-草堂\\d{1,2}-\\d{1,3}").matcher(rawStr).find()) {
            rawStr = rawStr.replaceAll("草堂校区-草堂校区\\d{1,2}号楼-", "");
        }
        // 草堂校区-草堂14号楼-14-101
        if (Pattern.compile("草堂校区-草堂\\d{1,2}号楼-\\d{1,2}-\\d{1,3}").matcher(rawStr).find()) {
            rawStr = rawStr.replaceAll("号楼-\\d{1,2}", "").substring(5);
        }
        // 草堂校区-学府城1-504-学府城1-504
        if (Pattern.compile("草堂校区-(学府城\\d{1,2}-\\d{3}-?){2}").matcher(rawStr).find()) {
            rawStr = rawStr.replaceAll("草堂校区-学府城\\d{1,2}-\\d{3}-", "");
        }
        // 草堂校区-16-405-16-405
        if (Pattern.compile("草堂校区-(\\d{1,2}-\\d{3}-?){2}").matcher(rawStr).find()) {
            rawStr = rawStr.replaceAll("校区-\\d{1,2}-\\d{3}-", "");
        }
        //草堂校区-13-13-315
        if (Pattern.compile("草堂校区-(\\d{1,2}-){2}\\d{1,3}").matcher(rawStr).find()) {
            rawStr = rawStr.replaceAll("校区-\\d{1,2}-", "");
        }
        // 雁塔校区-环境学院716-环境学院716
        if (Pattern.compile("雁塔校区-(环境学院\\d+-?){2}").matcher(rawStr).find()) {
            rawStr = rawStr.replaceAll("雁塔校区-环境学院\\d+-", "");
        }
        return rawStr.replace("雁塔校区-土木楼-", "")
                .replace("雁塔校区-教学大楼-", "")
                .replace("雁塔校区-南阶-", "")
                .replace("雁塔校区-东阶-", "")
                .replace("雁塔校区-西阶-", "")
                .replace("雁塔校区-教学大楼7楼中厅-", "")
                .replace("雁塔校区-东楼会议室A-", "")
                .replace("雁塔校区-建筑学院东楼-", "")
                .replace("雁塔校区-东楼-", "")
                .replace("雁塔校区-工科楼-", "")
                .replace("雁塔校区-青教-", "")
                .replace("雁塔校区-逸夫楼-", "")
                .replace("雁塔校区-膜院三楼会议室-", "");
    }

    @Nullable
    @Override
    public TimeTable generateTimeTable() {
        if (timeTable != null) {
            return timeTable;
        }
        Document document = Jsoup.parse(getSource());
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        Element tbody = document.select("table.table:nth-child(5) > tbody:nth-child(1)").get(0);
        Elements header = tbody.select("tr:nth-child(1) > td");
        HashMap<String, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < header.size(); i++) {
            if (header.get(i).text().contains("雁塔") && header.get(i).text().contains("冬")) {
                indexMap.put("雁塔冬季", i);
            } else if (header.get(i).text().contains("雁塔") && header.get(i).text().contains("夏")) {
                indexMap.put("雁塔夏季", i);
            } else if (header.get(i).text().contains("草堂")) {
                indexMap.put("草堂", i);
            }
        }
        Elements trs = tbody.select("tr#swsjbzlb1");
        String district = null;
        // 草堂不分冬夏
        if (jsInitFunc.contains("草堂")) {
            district = "草堂";
        }
        // 雁塔夏季作息
        else if (jsInitFunc.contains("雁塔") && month >= 5 && month <= 9) {
            district = "雁塔夏季";
        }
        // 雁塔冬季作息
        else if (jsInitFunc.contains("雁塔") && (month < 5 || month > 9)) {
            district = "雁塔冬季";
        }
        // 如果课表里上课地点没有校区信息，则默认使用草堂的时间表
        if (district == null) {
            district = "草堂";
        }
        List<TimeDetail> timeDetailList = generateTimeList(trs, indexMap, district);
        maxNode = timeDetailList.size();
        return new TimeTable(district, timeDetailList);
    }

    private List<TimeDetail> generateTimeList(Elements trs, HashMap<String, Integer> indexMap, String district) {
        if (!jsInitFunc.contains(district)) {
            return defaultTimeList(district);
        }
        return timeDetailParser(trs, indexMap.get(district));
    }

    /**
     * 处理每个校区的时间
     *
     * @param trs   整个表格tbody以内的部分
     * @param index 校区对应的列
     * @return 该校区的时间表
     */
    private List<TimeDetail> timeDetailParser(Elements trs, int index) {
        List<TimeDetail> timeDetailList = new ArrayList<>();
        for (int i = 0; i < trs.size(); i++) {
            Element tr = trs.get(i);
            String[] times = tr.select("td:nth-child(" + index + ") > font").text().split("-");
            // HTML的表格里有空行，用前一节课增加5分钟课间来作为这节课的开始时间
            // 这种课统一按45分钟算，因为草堂每节课45分钟，HTML空格对应的草堂可能是有课的，而雁塔都是实实在在没有课
            if (times.length != 2) {
                timeDetailList.add(
                        new TimeDetail(i + 1,
                                moveMinute(timeDetailList.get(i - 1).getEndTime(), "HH:mm", 5),
                                moveMinute(timeDetailList.get(i - 1).getEndTime(), "HH:mm", 45)));
                continue;
            }
            timeDetailList.add(
                    new TimeDetail(i + 1, addHourPrefixZero(times[0]), addHourPrefixZero(times[1])));
        }
        return timeDetailList;
    }

    private List<TimeDetail> defaultTimeList(String district) {
        List<TimeDetail> defaultTimeList = new ArrayList<>();
        // 草堂默认作息
        if ("草堂".equals(district)) {
            defaultTimeList.add(new TimeDetail(1, "08:30", "09:15"));
            defaultTimeList.add(new TimeDetail(2, "09:20", "10:05"));
            defaultTimeList.add(new TimeDetail(3, "10:25", "11:10"));
            defaultTimeList.add(new TimeDetail(4, "11:15", "12:00"));
            defaultTimeList.add(new TimeDetail(5, "12:10", "12:55"));
            defaultTimeList.add(new TimeDetail(6, "13:00", "13:45"));
            defaultTimeList.add(new TimeDetail(7, "14:00", "14:45"));
            defaultTimeList.add(new TimeDetail(8, "14:50", "15:35"));
            defaultTimeList.add(new TimeDetail(9, "15:45", "16:30"));
            defaultTimeList.add(new TimeDetail(10, "16:35", "17:20"));
            defaultTimeList.add(new TimeDetail(11, "19:30", "20:15"));
            defaultTimeList.add(new TimeDetail(12, "20:20", "21:05"));
        }
        // 雁塔冬季默认作息
        if ("雁塔冬季".equals(district)) {
            defaultTimeList.add(new TimeDetail(1, "08:40", "08:50"));
            defaultTimeList.add(new TimeDetail(2, "08:55", "09:50"));
            defaultTimeList.add(new TimeDetail(3, "10:10", "11:00"));
            defaultTimeList.add(new TimeDetail(4, "11:10", "12:00"));
            // 雁塔中午不上课，所以作息表上没有时间，但是为了和草堂统一，第5-6节还是空出来的（学校这么安排的），这里时间是随便写的，雁塔夏季作息同理
            defaultTimeList.add(new TimeDetail(5, "12:10", "12:55"));
            defaultTimeList.add(new TimeDetail(6, "13:00", "13:45"));
            defaultTimeList.add(new TimeDetail(7, "14:00", "14:50"));
            defaultTimeList.add(new TimeDetail(8, "15:00", "15:50"));
            defaultTimeList.add(new TimeDetail(9, "16:00", "16:50"));
            defaultTimeList.add(new TimeDetail(10, "17:00", "17:50"));
            defaultTimeList.add(new TimeDetail(11, "19:30", "20:20"));
            defaultTimeList.add(new TimeDetail(12, "20:30", "21:20"));
        }
        // 雁塔夏季默认作息
        if ("雁塔夏季".equals(district)) {
            defaultTimeList.add(new TimeDetail(1, "08:00", "08:50"));
            defaultTimeList.add(new TimeDetail(2, "08:55", "09:50"));
            defaultTimeList.add(new TimeDetail(3, "10:10", "11:00"));
            defaultTimeList.add(new TimeDetail(4, "11:10", "12:00"));
            defaultTimeList.add(new TimeDetail(5, "12:10", "12:55"));
            defaultTimeList.add(new TimeDetail(6, "13:00", "13:45"));
            defaultTimeList.add(new TimeDetail(7, "14:30", "15:20"));
            defaultTimeList.add(new TimeDetail(8, "15:30", "16:20"));
            defaultTimeList.add(new TimeDetail(9, "16:30", "17:20"));
            defaultTimeList.add(new TimeDetail(10, "17:30", "18:20"));
            defaultTimeList.add(new TimeDetail(11, "20:00", "20:50"));
            defaultTimeList.add(new TimeDetail(12, "21:00", "21:50"));
        }
        return defaultTimeList;
    }

    // 给时间增加前导零
    private String addHourPrefixZero(String time) {
        if (!time.startsWith("0") && time.length() == 4) {
            return "0" + time;
        }
        return time;
    }

    /**
     * 获取"HH:mm"增加minute分钟后的时间
     *
     * @param inputTime 格式为"HH:mm"的时间
     * @param minute    增肌的时间，格式同上
     * @return 增加时间后的时间
     */
    private String moveMinute(String inputTime, String pattern, int minute) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            Date date = sdf.parse(inputTime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.MINUTE, minute);
            Date newDate = calendar.getTime();
            return sdf.format(newDate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String moveDay(String inputDate, String pattern, int day) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            Date date = sdf.parse(inputDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DATE, day);
            Date newDate = calendar.getTime();
            return sdf.format(newDate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

