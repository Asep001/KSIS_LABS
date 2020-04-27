package sample;

public class ComboBoxItem {
    public Long id;
    public String name;


    public ComboBoxItem(String name,Long id){
        this.id =id;
        this.name = name;
    }

    @Override
    public String toString() {
        return  name;
    }
}
