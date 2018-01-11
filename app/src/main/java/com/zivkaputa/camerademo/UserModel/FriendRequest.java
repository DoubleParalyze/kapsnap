package com.zivkaputa.camerademo.UserModel;

/**
 * FriendRequest
 *
 * Class representing a single friend request
 */
public class FriendRequest{

    public String time;
    public String senderID;
    public String senderUsername;
    public String senderFullName;
    public boolean accepted;

    /**
     * Default constructor, required for Firebase
     */
    public FriendRequest(){

    }

    /**
     * Constructor for FriendRequest object
     *
     * @param sender - The sender's ID
     * @param senderUsername - The sender's username
     * @param senderFullName - The sender's full name
     * @param time - The time the request was sent
     */
    public FriendRequest( String sender, String senderUsername, String senderFullName, String time ){
        this.time = time;
        this.senderID = sender;
        this.accepted = false;
        this.senderUsername = senderUsername;
        this.senderFullName = senderFullName;
    }


    /**
     * Overidden equals operator
     *
     * @param obj
     * @return boolean indicating whther the objects are equal
     */
    @Override
    public boolean equals( Object obj ) {

        if ( obj == null ) {
            return false;
        }

        if ( !(obj instanceof FriendRequest) ) {
            return false;
        }

        final FriendRequest other = (FriendRequest) obj;

        return  this.senderID == other.senderID &&
                this.time == other.time;

    }


}
