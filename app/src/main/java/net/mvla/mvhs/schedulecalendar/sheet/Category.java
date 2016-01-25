
package net.mvla.mvhs.schedulecalendar.sheet;

import java.util.HashMap;
import java.util.Map;


public class Category {

    private String scheme;
    private String term;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The scheme
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * @param scheme The scheme
     */
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    /**
     * @return The term
     */
    public String getTerm() {
        return term;
    }

    /**
     * @param term The term
     */
    public void setTerm(String term) {
        this.term = term;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
