package ma.snrt.news.model;

import java.io.Serializable;

import ma.snrt.news.AppController;

public class CategoryAgenda implements Serializable {

    private int id;
    private String title;
    private String color;
    private String image;
    private boolean isSelected;

    public CategoryAgenda() {
        this.id = 0;
    }

    public CategoryAgenda(int id, String title, String color) {
        this.id = id;
        this.title = title;
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getImage() {
        String result = "";
        if(image!=null && !image.contains("http")) {
            result = AppController.BASE_URL + image;
        }
        else if(image!=null && image.contains("http"))
            result = image;
        return result;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
