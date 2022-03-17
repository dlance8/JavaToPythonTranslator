package examples;

public class testing {
    private static void classMethod(){
        System.out.println("World");
    }
    public static void main(String[] args){
        int z = 3;
        System.out.println("hello");
        int y = 5;
        classMethod();

        int x = 4;

        if(x < 5){
            System.out.println("x < 5");
        }
        else {
            System.out.println("x > 5");
        }
    }
}
