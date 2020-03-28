package sample;

public class ComboBoxItem {
    public int id;
    public String name;


    public ComboBoxItem(String name,int id){
        this.id =id;
        this.name = name;
    }

    @Override
    public String toString() {
        return  name;
    }
}
