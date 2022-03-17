package examples;

public class classMethods {
    private static void test(){
        System.out.print("test function");
        test2();
    }

    private static void test2(){
        System.out.println("test2");
    }

    public static void main(String[] args){
        System.out.println("Hello");
        test();
    }
}


