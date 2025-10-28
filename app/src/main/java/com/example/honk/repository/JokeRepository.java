package com.example.honk.repository;

import android.util.JsonReader;

import androidx.annotation.NonNull;

import com.example.honk.data.jokes.CategoriesEnum;
import com.example.honk.data.jokes.FlagsEnum;
import com.example.honk.data.jokes.ParamsEntity;
import com.example.honk.ui.categories.Category;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;

public class JokeRepository {
    private static String api = "https://v2.jokeapi.dev/joke/";

    public static String getJoke(String contains) {
        ParamsEntity params = new ParamsEntity(contains);
        api += generateRequestString(params);

        try{
            HttpURLConnection con = (HttpURLConnection) new URL(api).openConnection();
            con.setRequestProperty("accept", "application/json");
            con.setRequestMethod("GET");

            String ans = getString(con);
            JSONObject obj = new JSONObject(ans);

            boolean errored = obj.getBoolean("error");

            if (errored){
                return "";
            }

            return obj.getString("joke");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    @NonNull
    private static String getString(HttpURLConnection con) {
        String ans = "";

        try (InputStream resp = con.getInputStream();
             InputStreamReader isr = new InputStreamReader(resp);
             BufferedReader reader = new BufferedReader(isr)) {

            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            ans = sb.toString();
        } catch (IOException e){
            ans = "";
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return ans;
    }

    private static String generateRequestString(ParamsEntity params){
        StringBuilder category = new StringBuilder();
        if (Arrays.stream(params.categories).anyMatch(s -> s == CategoriesEnum.ANY)){
            category = new StringBuilder("Any,");
        }else{
            for (CategoriesEnum categoriesEnum : params.categories) {
                category.append(categoriesEnum.toString()).append(",");
            }
        }

        category.deleteCharAt(-1); //get rid of the coma

        String language = ""; //we're probably only gonna be using english, which is the default
        StringBuilder flagsLink = new StringBuilder();
        FlagsEnum[] flags = params.flags;
        if(flags.length != 0){
            flagsLink.append("&");
            for (FlagsEnum flag : flags) {
                flagsLink.append(flag.toString()).append(",");
            }
        }

        flagsLink.deleteCharAt(-1);

        String jokeType = "&type=single"; //just a default for now
        String contains = params.contains.isEmpty() ? "" : "&"+params.contains;
        //wont touch amount for now, maybe not needed

        return category.toString() + language + flagsLink.toString() + jokeType + contains;
    }
}
