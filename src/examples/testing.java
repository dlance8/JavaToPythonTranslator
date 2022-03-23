package examples;

public class testing {
    private static void classMethod(){ // set indent 2
        System.out.println("World");
    }
    public static void main(String[] args){
        int z = 3;
        System.out.println("hello");
        int y = 5;
        classMethod();

        int x = 4;

        if(x < 5){
            String a = "a";
            System.out.println("x < 5");
        }
        else {
            String b = "b";
            System.out.println("x > 5");
        }

        for(int i = 0; i < 2; i++){
            System.out.println(i);
        }
    }
}
