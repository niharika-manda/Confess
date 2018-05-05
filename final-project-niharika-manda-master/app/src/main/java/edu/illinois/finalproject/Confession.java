package edu.illinois.finalproject;

/**
 * Created by Nikki on 12/6/17.
 */

public class Confession {
    private String Confession_Title;
    private String Confession_Description;
    private String Image;

    public String getConfession_Title() {
        return Confession_Title;
    }

    public String getConfession_Description() {
        return Confession_Description;
    }

    public String getImage() {
        return Image;
    }

    public void setConfession_Title(String confession_Title) {
        Confession_Title = confession_Title;
    }

    public void setConfession_Description(String confession_Description) {
        Confession_Description = confession_Description;
    }

    public void setImage(String image) {
        Image = image;
    }

    public Confession() {

    }

    public Confession(String confession_Title, String confession_Description, String image) {
        Confession_Title = confession_Title;
        Confession_Description = confession_Description;
        Image = image;
    }
}
