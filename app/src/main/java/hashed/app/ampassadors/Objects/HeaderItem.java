package hashed.app.ampassadors.Objects;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.lang.reflect.Array;
import java.util.ArrayList;

import hashed.app.ampassadors.Activities.PostNewsActivity;
import hashed.app.ampassadors.Objects.Course;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;

public class HeaderItem  {

    String type ;
    String id ;
    String title ;
    Object object ;

   public HeaderItem(String title , String type , String id , Object object){

       this.type = type ;
       this.id = id ;
       this.title = title;
        this.object = object;
   }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
