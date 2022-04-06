package examples;

public class objects {
    int modelYear;
    String modelName;

    public objects(){
        modelName = "Mustang";
        modelYear = 1969;
    }
    public static void main(String[] args){
        objects carObj = new objects();
        System.out.println(carObj.modelName);
        System.out.println(carObj.modelYear);
    }
}
