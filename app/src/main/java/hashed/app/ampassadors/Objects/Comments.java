package hashed.app.ampassadors.Objects;

public class Comments extends  PostData{
    String commentstext;
    String commentslikes;
    String commentforcomments;

    public String getCommentstext() {
        return commentstext;
    }

    public void setCommentstext(String commentstext) {
        this.commentstext = commentstext;
    }

    public String getCommentslikes() {
        return commentslikes;
    }

    public void setCommentslikes(String commentslikes) {
        this.commentslikes = commentslikes;
    }

    public String getCommentforcomments() {
        return commentforcomments;
    }

    public void setCommentforcomments(String commentforcomments) {
        this.commentforcomments = commentforcomments;
    }
}