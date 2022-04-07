package examples;

public class testing {
    int modelYear;
    String modelName;

    public testing(){
        modelName = "Mustang";
        modelYear = 1969;
    }
    public static void main(String[] args){
        testing carObj = new testing();
        System.out.println(carObj.modelName);
        System.out.println(carObj.modelYear);
    }
}
