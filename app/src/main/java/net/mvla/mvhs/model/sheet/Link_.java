
package net.mvla.mvhs.model.sheet;

import java.util.HashMap;
import java.util.Map;


public class Link_ {

    private String rel;
    private String type;
    private String href;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The rel
     */
    public String getRel() {
        return rel;
    }

    /**
     * @param rel The rel
     */
    public void setRel(String rel) {
        this.rel = rel;
    }

    /**
     * @return The type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type The type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return The href
     */
    public String getHref() {
        return href;
    }

    /**
     * @param href The href
     */
    public void setHref(String href) {
        this.href = href;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
