package com.example.honk.data.jokes;

public class ParamsEntity {
    public CategoriesEnum[] categories;
    public String language = "";
    public FlagsEnum[] flags;
    public String contains;
    public int amount;

    public ParamsEntity(String contains){
        this.categories = new CategoriesEnum[] { CategoriesEnum.ANY};
        this.flags = new FlagsEnum[0];
        this.contains = contains;
    }
}
