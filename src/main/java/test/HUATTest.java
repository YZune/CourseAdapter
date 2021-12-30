package main.java.test;

import main.java.parser.HUATParser;

public class HUATTest {

    public static void main (String[] args) {
        HUATParser parser = new HUATParser("<table id=\"ctl00_ContentPlaceHolder1_CourseTable\" cellspacing=\"0\" cellpadding=\"0\" rules=\"all\" border=\"1\"\n" +
                "    style=\"border-color:Black;border-width:1px;border-style:Solid;border-collapse:collapse;\"> 省略了...这次是311行的HTML </table>");
        System.out.println(parser.generateCourseList());
    }
}
