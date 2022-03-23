package examples;

import java.util.Scanner;

public class classMethods {

    private static void test(){
        System.out.print("test function");
        test2();
    }

    private static void test2(){
        System.out.println("test2");
    }

    public static void main(String[] args){
        Scanner scan = new Scanner(System.in);
        String x = scan.next();
        System.out.println("Hello");
        test();
    }
}


