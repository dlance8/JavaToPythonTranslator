//import java.util.Scanner;
//import java.io.IOException;

public class MyClass {
    public static void main(String[] args){
        System.out.println("Hello World");
//        Scanner scan = new Scanner(System.in);
//        System.out.print("Enter a number: ");
//        int number = scan.nextInt();
//        System.out.println("You entered: " + number);
    }

//    private void otherMethod(){
//        System.out.println("Hi World");
//    }
}


//class MyClass:
//    def main():
//        print("Hello World")


/* otherfunction():

needs to be translated:
variable assignment
if/else statements
for loops
while loops

how to handle main method?
-place if __name__ == '__main__': at bottom of code, and call main from there (probably the best implementation)
-place it within the class and call classname.main() at the end of the code
-define main at the bottom of the code and call main() afterwards


Potential implementations
------------------------
class myClass:
    def main():
        //main method body
        myClass.classMethod()

if __name__ == "__main__":
    main()

-------------------------

class myClass:
    def classMethod():
        // method body

myClass.classMethod()

-------------------------

class myClass:
    def classMethod():
        // method body

def main():
    // main method body
    myClass.classMethod()

main()

 */