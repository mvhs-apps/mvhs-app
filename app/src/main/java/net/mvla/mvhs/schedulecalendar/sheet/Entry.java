
package net.mvla.mvhs.schedulecalendar.sheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Entry {

    private Id_ id;
    private Updated_ updated;
    private List<Category_> category = new ArrayList<Category_>();
    private Title_ title;
    private Content content;
    private List<Link_> link = new ArrayList<Link_>();
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The id
     */
    public Id_ getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(Id_ id) {
        this.id = id;
    }

    /**
     * @return The updated
     */
    public Updated_ getUpdated() {
        return updated;
    }

    /**
     * @param updated The updated
     */
    public void setUpdated(Updated_ updated) {
        this.updated = updated;
    }

    /**
     * @return The category
     */
    public List<Category_> getCategory() {
        return category;
    }

    /**
     * @param category The category
     */
    public void setCategory(List<Category_> category) {
        this.category = category;
    }

    /**
     * @return The title
     */
    public Title_ getTitle() {
        return title;
    }

    /**
     * @param title The title
     */
    public void setTitle(Title_ title) {
        this.title = title;
    }

    /**
     * @return The content
     */
    public Content getContent() {
        return content;
    }

    /**
     * @param content The content
     */
    public void setContent(Content content) {
        this.content = content;
    }

    /**
     * @return The link
     */
    public List<Link_> getLink() {
        return link;
    }

    /**
     * @param link The link
     */
    public void setLink(List<Link_> link) {
        this.link = link;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
