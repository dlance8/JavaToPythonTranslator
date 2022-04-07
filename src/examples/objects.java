package examples;

public class objects {
    int modelYear;
    String modelName;

    public objects(String name, int year){
        modelName = name;
        modelYear = year;
    }
    public static void main(String[] args){
        objects car1 = new objects("Mustang", 1969);
        objects car2 = new objects("Tesla Model 3", 2022);

        System.out.println(car1.modelName);
        System.out.println(car1.modelYear);

        System.out.println(car2.modelName);
        System.out.println(car2.modelYear);
    }
}
