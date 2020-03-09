package ma.snrt.news.model;

import java.io.Serializable;
import java.util.ArrayList;

import ma.snrt.news.AppController;

public class User implements Serializable {

    private int id;
    private String name;
    private String image;
    private ArrayList<Story> stories;

    public User() {
    }

    public User(int id, String name, String image, ArrayList<Story> stories) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.stories = stories;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public ArrayList<Story> getStories() {
        return stories;
    }

    public void setStories(ArrayList<Story> stories) {
        this.stories = stories;
    }
}
