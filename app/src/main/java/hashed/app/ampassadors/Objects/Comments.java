package hashed.app.ampassadors.Objects;

import com.google.firebase.database.PropertyName;

public class Comments extends  PostData{

    @PropertyName("commentstext")
    String commentstext;
    @PropertyName("likesnumber")
    int likesnumber;
    @PropertyName("userid")
    String userid;
    @PropertyName("time")
    long time;
    @PropertyName("repliesnumber")
    int repliesnumber ;
    @PropertyName("commnetsid")
    String commnetsid ;
    @PropertyName("userlike")
    String []userlikes ;


    public String getCommnetsid() {
        return commnetsid;
    }

    public void setCommnetsid(String commnetsid) {
        this.commnetsid = commnetsid;
    }

    public int getLikesnumber() {
        return likesnumber;
    }

    public void setLikesnumber(int likesnumber) {
        this.likesnumber = likesnumber;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getRepliesnumber() {
        return repliesnumber;
    }

    public void setRepliesnumber(int repliesnumber) {
        this.repliesnumber = repliesnumber;
    }

    public String getCommentstext() {
        return commentstext;
    }

    public void setCommentstext(String commentstext) {
        this.commentstext = commentstext;
    }

    public String[] getUserlikes() {
        return userlikes;
    }

    public void setUserlikes(String[] userlikes) {
        this.userlikes = userlikes;
    }

}