
package net.mvla.mvhs.backend.model.sheet;

import java.util.HashMap;
import java.util.Map;


public class Author {

    private Name name;
    private Email email;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The name
     */
    public Name getName() {
        return name;
    }

    /**
     * @param name The name
     */
    public void setName(Name name) {
        this.name = name;
    }

    /**
     * @return The email
     */
    public Email getEmail() {
        return email;
    }

    /**
     * @param email The email
     */
    public void setEmail(Email email) {
        this.email = email;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
