
package net.mvla.mvhs.backend.model.sheet;

import java.util.HashMap;
import java.util.Map;


public class RootSheetElement {

    private String version;
    private String encoding;
    private Feed feed;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version The version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return The encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * @param encoding The encoding
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * @return The feed
     */
    public Feed getFeed() {
        return feed;
    }

    /**
     * @param feed The feed
     */
    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
