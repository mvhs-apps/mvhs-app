
package net.mvla.mvhs.model.event;

import java.util.HashMap;
import java.util.Map;

public class Start {

    private String dateTime;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The dateTime
     */
    public String getDateTime() {
        return dateTime;
    }

    /**
     * @param dateTime The dateTime
     */
    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
