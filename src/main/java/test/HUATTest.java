package main.java.test;

import main.java.parser.HUATParser;

public class HUATTest {

    public static void main (String[] args) {
        HUATParser parser = new HUATParser("<table> 此处省略700多行的table内容 </table>");
        System.out.println(parser.generateCourseList());
    }
}
