package hashed.app.ampassadors.Objects;

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
