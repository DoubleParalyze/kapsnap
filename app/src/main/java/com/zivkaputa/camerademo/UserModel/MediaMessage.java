package com.zivkaputa.camerademo.UserModel;


/**
 * MediaMessage
 *
 * Class for representing a single message
 */
public class MediaMessage {

    public String sender;
    public String recipient;
    public MediaType type;
    public String path;
    public String time;
    public int duration;
    public boolean opened;
    public boolean incoming;

    /**
     * Enumeration used for the media type
     */
    public enum MediaType {
        PHOTO, VIDEO
    }

    /**
     * Default constructor
     */
    public MediaMessage(){

    }

    /**
     * Constructor for MediaMessage object
     *
     * @param sender - The message sender
     * @param recipient - The message recipient
     * @param path - The media path
     * @param type - The media type
     * @param time - The time the media was sent
     * @param duration - The media duration
     */
    public MediaMessage( String sender, String recipient, String path, MediaType type, String time, int duration ){


        this.sender = sender;
        this.recipient = recipient;
        this.type = type;
        this.path = path;
        this.duration = duration;
        this.time = time;
        this.opened = false;


    }


    /**
     * Overidden equals method
     *
     * @param obj
     * @return boolean indicating whether the objects are equal
     */
    @Override
    public boolean equals( Object obj ) {

        if ( obj == null ) {
            return false;
        }

        if ( !(obj instanceof MediaMessage) ) {
            return false;
        }

        final MediaMessage other = (MediaMessage) obj;

        return  this.sender == other.sender &&
                this.recipient == other.recipient &&
                this.type == other.type &&
                this.path == other.path &&
                this.duration == other.duration &&
                this.time == other.time;

    }





}
