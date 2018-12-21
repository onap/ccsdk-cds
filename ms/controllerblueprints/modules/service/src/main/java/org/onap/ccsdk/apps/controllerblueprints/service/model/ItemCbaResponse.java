package org.onap.ccsdk.apps.controllerblueprints.service.model;

/**
 * CLass that would represent the response for the GET methods on the CBAService class
 */
public class ItemCbaResponse {

    private String id;
    private String description;
    private String name;
    private int state;
    private String version;

    public ItemCbaResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


}
