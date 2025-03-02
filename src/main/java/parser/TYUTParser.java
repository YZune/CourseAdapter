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


/**
 * 邮箱：xingyuan_guo@outlook.com
 * @author Ez
 */

public class TYUTParser extends Parser {

    private String finalSource;

    public TYUTParser(@NotNull String source) {
        super(source);
        String start_Tag = "<tbody>";
        String end_Tag = "</tbody>";
        int pos_start = source.lastIndexOf(start_Tag);
        int pos_end = source.lastIndexOf(end_Tag);
        this.finalSource = source.substring(pos_start,pos_end+8).trim();
    }

    @NotNull
    @Override
    public List<Course> generateCourseList() {
        Document doc = Jsoup.parse("<table>"+this.finalSource+"</table>");
        Elements rows = doc.select("tr");
        List<Course> courseList = new ArrayList<>();
        for (Element row : rows) {
            Elements cols = row.select("td");
            String courseName = cols.get(2).text();
            int day = Integer.parseInt(cols.get(10).text());
            String room = cols.get(12).text()+" "+cols.get(13).text()+" "+cols.get(14).text();
            String teacher = cols.get(8).text();
            String s2e = cols.get(11).text();
            String[] s2es = s2e.split("-");
            int startNode = Integer.parseInt(s2es[0]);
            int endNode = Integer.parseInt(s2es[1]);
            String week = cols.get(9).text().replace("周","");
                String[] weeks = week.split("-");
                startWeek = Integer.parseInt(weeks[0]);
                endWeek = Integer.parseInt(weeks[1]);
                if (Integer.parseInt(weeks[0])%2==1){
                    //单周
                    type = 1;
                    startWeek = Integer.parseInt(weeks[0]);
                    endWeek = Integer.parseInt(weeks[weeks.length-1]);
                }else{
                    //双周
                    type = 2;
                    startWeek = Integer.parseInt(weeks[0]);
                    endWeek = Integer.parseInt(weeks[weeks.length-1]);
                }
            }
            float credit = Float.parseFloat(cols.get(4).text());
            courseList.add(course);
        }
        return courseList;
    }
}
