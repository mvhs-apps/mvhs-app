
package net.mvla.mvhs.backend.model.sheet;

import java.util.HashMap;
import java.util.Map;


public class Id_ {

    private String $t;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The $t
     */
    public String get$t() {
        return $t;
    }

    /**
     * @param $t The $t
     */
    public void set$t(String $t) {
        this.$t = $t;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
