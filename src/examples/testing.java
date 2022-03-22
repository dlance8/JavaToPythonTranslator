package examples;

public class testing { // new line and set indent 1
    private static void classMethod(){ // set indent 2
        System.out.println("World");
    } // set indent 1
    public static void main(String[] args){ //set indent 2 and newline
        int z = 3; // newline
        System.out.println("hello"); //newline
        int y = 5; //newline
        classMethod(); //newline

        int x = 4; //new line

        if(x < 5){ // increaseIndent and newline
            String a = "a";
            System.out.println("x < 5"); // newline
        } //decrease indent and newline
        else { // increase indent and newline
            String b = "b";
            System.out.println("x > 5"); // newline
        } //decrease indent and newline

        for(int i = 0; i < 2; i++){ //increase indent and newline
            System.out.println(i); //newline
        }//decrease indent and newline
    }
}
