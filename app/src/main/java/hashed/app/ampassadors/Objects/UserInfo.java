package hashed.app.ampassadors.Objects;

import com.google.firebase.database.PropertyName;

public class UserInfo {

  @PropertyName("username")
  String username;
  @PropertyName("password")
  String password;
  @PropertyName("email")
  String email;
  @PropertyName("country")
  String country;
  @PropertyName("city")
  String city;
  @PropertyName("phone")
  String phone;
  @PropertyName("imageUrl")
  String imageUrl;
  @PropertyName("userid")
  String userid;
  @PropertyName("status")
  boolean status;
  String userRole;
  boolean approvement;

  public UserInfo() {
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }


  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public String getUserid() {
    return userid;
  }

  public void setUserid(String userid) {
    this.userid = userid;
  }

  //

  public Boolean getStatus() {
    return status;
  }

  public void setStatus(Boolean status) {
    this.status = status;
  }


  public String getUserRole() {
    return userRole;
  }

  public void setUserRole(String userRole) {
    this.userRole = userRole;
  }


  public Boolean getApprovement() {
    return approvement;
  }

  public void setApprovement(boolean approvement) {
    this.approvement = approvement;
  }
}
