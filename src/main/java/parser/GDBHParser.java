package main.java.parser;

import bean.Course;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import main.java.bean.TimeDetail;
import main.java.bean.TimeTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import parser.Parser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/*
 * 适用于桂林电子科技大学北海校区192.168.5.88教务系统
 * 仅支持校园网内访问
 * @Author: Dango
 * @Author: Moluer
 * GDBH NetCen 2024/12/21
 */
public class GDBHParser extends Parser {
    private JsonArray courseArray;
    private final String cookie;

    public GDBHParser(@NotNull String source, String cookie) {
        super();
        this.cookie = cookie;
    }

    @Nullable
    @Override
    public TimeTable generateTimeTable() {
        List<TimeDetail> timeList = List.of(
                new TimeDetail(1, "08:25", "09:10"),
                new TimeDetail(2, "09:20", "10:05"),
                new TimeDetail(3, "10:25", "11:10"),
                new TimeDetail(4, "11:20", "12:05"),
                new TimeDetail(5, "15:00", "15:45"),
                new TimeDetail(6, "15:55", "16:40"),
                new TimeDetail(7, "17:00", "17:45"),
                new TimeDetail(8, "17:55", "18:40"),
                new TimeDetail(9, "19:40", "20:25"),
                new TimeDetail(10, "20:35", "21:20")
        );
        return new TimeTable("桂电北海", timeList);
    }

    @NotNull
    @Override
    public List<Course> generateCourseList() {
        List<Course> courseList = new ArrayList<>();
        getCourseList();
        for (int i = 0; i < courseArray.size(); i++) {
            JsonObject courseObject = courseArray.get(i).getAsJsonObject();
            courseList.add(addCourse(courseObject));
        }
        return courseList;
    }

    private Course addCourse(JsonObject courseObject) {
        String name = courseObject.get("cname").getAsString();
        int day = courseObject.get("week").getAsInt();
        String room = courseObject.has("croomno") && !courseObject.get("croomno").isJsonNull() && !courseObject.get("croomno").getAsString().isEmpty()
                ? courseObject.get("croomno").getAsString() : "NULL";
        String teacher = courseObject.get("name").getAsString();
        int seq = courseObject.get("seq").getAsInt();
        int startNode = resolveBeginTime(seq);
        int endNode = resolveEndTime(seq);
        int startWeek = courseObject.get("startweek").getAsInt();
        int endWeek = courseObject.get("endweek").getAsInt();
        float credit = courseObject.get("xf").getAsFloat();
        return new Course(name, day, room, teacher, startNode, endNode, startWeek, endWeek, 0, credit, "", "", "", );
    }

    private int resolveBeginTime(int seq) {
        return switch (seq) {
            case 1 -> 1;
            case 2 -> 3;
            case 3 -> 5;
            case 4 -> 7;
            case 5 -> 9;
            case 6 -> 11;
            default -> 0;
        };
    }

    private int resolveEndTime(int seq) {
        return switch (seq) {
            case 1 -> 2;
            case 2 -> 4;
            case 3 -> 6;
            case 4 -> 8;
            case 5 -> 10;
            case 6 -> 12;
            default -> 0;
        };
    }

    private String sendGetRequest(String url) {
        StringBuilder result = new StringBuilder();
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Cookie", this.cookie);
            con.setRequestProperty("Referer", "http://192.168.5.88/Login/MainDesktop");
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        result.append(inputLine);
                    }
                }
            } else {
                throw new RuntimeException("Failed : HTTP error code : " + con.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public String getSemester() {
        String url = "http://192.168.5.88/student/StuInfo";
        String response = sendGetRequest(url);
        JsonReader reader = new JsonReader(new StringReader(response));
        reader.setLenient(true);
        JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
        return jsonObject.get("term").getAsString();
    }

    public void getCourseList() {
        String url = "http://192.168.5.88/student/getstutable?_dc=" + System.currentTimeMillis() + "&term=" + getSemester() + "&page=1&start=0&limit=25";
        String response = sendGetRequest(url);
        JsonReader reader = new JsonReader(new StringReader(response));
        reader.setLenient(true);
        JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
        this.courseArray = jsonObject.getAsJsonArray("data");
    }

    public JsonArray test() {
        return courseArray;
    }
}