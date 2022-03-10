public class MyTestClass {
    private static void test(){
        System.out.print("test function");
    }

    private static void test2(){
        test();
    }


    public static void main(String[] args){
        System.out.println("Hello World");
        test();
//        for (int i = 0; i < 5; ++i){
//            System.out.print(i);
//        }
    }
}