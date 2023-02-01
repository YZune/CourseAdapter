package main.java.test;

import main.java.parser.HUATParser;

public class HUATTest {

    public static void main (String[] args) {
        HUATParser parser = new HUATParser(" </table>");
        System.out.println(parser.generateCourseList());

    }
}
