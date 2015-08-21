
package net.mvla.mvhs.model.event;

import java.util.HashMap;
import java.util.Map;

public class Organizer {

    private String email;
    private String displayName;
    private boolean self;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email The email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return The displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName The displayName
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return The self
     */
    public boolean isSelf() {
        return self;
    }

    /**
     * @param self The self
     */
    public void setSelf(boolean self) {
        this.self = self;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
