package com.danielmerrill.gettingwarmer;

/**
 * Created by danielmerrill on 6/16/16.
 */




        import java.util.ArrayList;
        import java.util.List;
        //import javax.annotation.Generated;
        import com.google.gson.annotations.Expose;
        import com.google.gson.annotations.SerializedName;

//@Generated("org.jsonschema2pojo")
public class LoginModel {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("login")
    @Expose
    private String login;
    @SerializedName("latitude_target")
    @Expose
    private String latitudeTarget;
    @SerializedName("longitude_target")
    @Expose
    private String longitudeTarget;
    @SerializedName("latitude_start")
    @Expose
    private String latitudeStart;
    @SerializedName("longitude_start")
    @Expose
    private String longitudeStart;
    @SerializedName("is_new")
    @Expose
    private String isNew;
    @SerializedName("friends")
    @Expose
    private List<String> friends = new ArrayList<String>();
    @SerializedName("friendsWithNewLocation")
    @Expose
    private List<String> friendsWithNewLocation = new ArrayList<String>();
    @SerializedName("requests")
    @Expose
    private List<String> requests = new ArrayList<String>();

    /**
     *
     * @return
     * The status
     */
    public String getStatus() {
        return status;
    }

    /**
     *
     * @param status
     * The status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     *
     * @return
     * The login
     */
    public String getLogin() {
        return login;
    }

    /**
     *
     * @param login
     * The login
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     *
     * @return
     * The latitudeTarget
     */
    public String getLatitudeTarget() {
        return latitudeTarget;
    }

    /**
     *
     * @param latitudeTarget
     * The latitude_target
     */
    public void setLatitudeTarget(String latitudeTarget) {
        this.latitudeTarget = latitudeTarget;
    }



    /**
     *
     * @return
     * The longitudeTarget
     */
    public String getLongitudeTarget() {
        return longitudeTarget;
    }

    /**
     *
     * @param longitudeTarget
     * The longitude_target
     */
    public void setLongitudeTarget(String longitudeTarget) {
        this.longitudeTarget = longitudeTarget;
    }

    /**
     *
     * @return
     * The latitudeStart
     */
    public String getLatitudeStart() {
        return latitudeStart;
    }

    /**
     *
     * @param latitudeStart
     * The latitude_start
     */
    public void setLatitudeStart(String latitudeStart) {
        this.latitudeStart = latitudeStart;
    }

    /**
     *
     * @return
     * The longitudeStart
     */
    public String getLongitudeStart() {
        return longitudeStart;
    }

    /**
     *
     * @param longitudeStart
     * The longitude_start
     */
    public void setLongitudeStart(String longitudeStart) {
        this.longitudeStart = longitudeStart;
    }

    /**
     *
     * @return
     * The is new token
     */
    public String getIsNew() {
        return isNew;
    }

    /**
     *
     * @param isNew
     * The is_new token
     */
    public void setIsNew(String isNew) {
        this.isNew = isNew;
    }

    /**
     *
     * @return
     * List of all friends
     */
    public List<String> getFriends() {
        return friends;
    }


    /**
     *
     * @param friends
     * The friends
     */
    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    /**
     *
     * @return
     * The requests
     */
    public List<String> getRequests() {
        return requests;
    }

    /**
     *
     * @param requests
     * The requests
     */
    public void setRequests(List<String> requests) {
        this.requests = requests;
    }

    /**
     *
     * @return
     * List of friends who have a new location
     */
    public List<String> getFriendsWithNewLocation() {
        return friendsWithNewLocation;
    }

    /**
     *
     * @param friendsWithNewLocation
     * Friends with new location
     */
    public void setFriendsWithNewLocation(List<String> friendsWithNewLocation) {
        this.friendsWithNewLocation = friendsWithNewLocation;
    }

}