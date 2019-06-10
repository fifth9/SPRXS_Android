package com.divya.sprxs.model;

public class CreateIdeasResponse {


    private String createIdea_response;
    private String idea_name;
    private String filename;
    private String tokenID;
    private String createIdea_message;
    private String lkp_email;
    private String idea_ID;
    private String mintedBy;


    public CreateIdeasResponse(String createIdea_response, String idea_name, String filename, String tokenID, String createIdea_message, String lkp_email, String idea_ID, String mintedBy) {
        this.createIdea_response = createIdea_response;
        this.idea_name = idea_name;
        this.filename = filename;
        this.tokenID = tokenID;
        this.createIdea_message = createIdea_message;
        this.lkp_email = lkp_email;
        this.idea_ID = idea_ID;
        this.mintedBy = mintedBy;
    }

    public String getCreateIdea_response() {
        return createIdea_response;
    }

    public String getIdea_name() {
        return idea_name;
    }

    public String getFilename() {
        return filename;
    }

    public String getTokenID() {
        return tokenID;
    }

    public String getCreateIdea_message() {
        return createIdea_message;
    }

    public String getLkp_email() {
        return lkp_email;
    }

    public String getIdea_ID() {
        return idea_ID;
    }

    public String getMintedBy() {
        return mintedBy;
    }
}