package ma.snrt.news.model;

import java.io.Serializable;

import ma.snrt.news.AppController;

public class Post implements Serializable {

    private int id;
    private String title;
    private String date_publication;
    private String description_article;
    private String image;
    private int is_featured;
    private String position;
    private String updateDate;
    private String category;
    private String tags;
    private String author;
    private String color;
    private String image_portrait;
    private String url;
    private String link;
    private String type;
    private int likes_numbers;
    private String duration;
    private String field_resume;
    private String date;
    private String description;
    private boolean isSelected;
    private int live;
    private int customCategory;
    private String type_contenu;
    private String text_speech;
    private String resume;

    public Post() {
    }

    public Post(int customCategory) {
        this.customCategory = customCategory;
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

    public String getDatePublication() {
        return date_publication;
    }

    public void setDatePublication(String datePublication) {
        this.date_publication = datePublication;
    }

    public String getDescriptionArticle() {
        return description_article;
    }

    public void setDescriptionArticle(String descriptionArticle) {
        this.description_article = descriptionArticle;
    }

    public String getImage() {
        String result = "";
        if(image!=null && !image.contains("http")) {
            result = AppController.BASE_URL + image;
        }
        return result;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getIsFeatured() {
        return is_featured;
    }

    public void setIsFeatured(int isFeatured) {
        this.is_featured = isFeatured;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getImagePortrait() {
        String result = "";
        if(image_portrait!=null && !image_portrait.isEmpty())
            result = AppController.BASE_URL + image_portrait;
        return result;
    }

    public void setImagePortrait(String fieldImagePortrait) {
        this.image_portrait = fieldImagePortrait;
    }

    public String getUrl() {
        if(url!=null && !url.contains("http"))
            url = AppController.BASE_URL + url;
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLikes_numbers() {
        return likes_numbers;
    }

    public void setLikes_numbers(int likes_numbers) {
        this.likes_numbers = likes_numbers;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getField_resume() {
        return field_resume;
    }

    public void setField_resume(String field_resume) {
        this.field_resume = field_resume;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getLive() {
        return live;
    }

    public void setLive(int live) {
        this.live = live;
    }

    public int getCustomCategory() {
        return customCategory;
    }

    public void setCustomCategory(int customCategory) {
        this.customCategory = customCategory;
    }

    public String getType_contenu() {
        return type_contenu;
    }

    public void setType_contenu(String type_contenu) {
        this.type_contenu = type_contenu;
    }

    public String getText_speech() {
        return text_speech;
    }

    public void setText_speech(String text_speech) {
        this.text_speech = text_speech;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }
}
