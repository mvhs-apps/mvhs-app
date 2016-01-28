
package net.mvla.mvhs.backend.model.sheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Feed {

    private String xmlns;
    private String xmlns$openSearch;
    private String xmlns$batch;
    private String xmlns$gs;
    private Id id;
    private Updated updated;
    private List<Category> category = new ArrayList<Category>();
    private Title title;
    private List<Link> link = new ArrayList<Link>();
    private List<Author> author = new ArrayList<Author>();
    private OpenSearch$totalResults openSearch$totalResults;
    private OpenSearch$startIndex openSearch$startIndex;
    private List<Entry> entry = new ArrayList<Entry>();
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The xmlns
     */
    public String getXmlns() {
        return xmlns;
    }

    /**
     * @param xmlns The xmlns
     */
    public void setXmlns(String xmlns) {
        this.xmlns = xmlns;
    }

    /**
     * @return The xmlns$openSearch
     */
    public String getXmlns$openSearch() {
        return xmlns$openSearch;
    }

    /**
     * @param xmlns$openSearch The xmlns$openSearch
     */
    public void setXmlns$openSearch(String xmlns$openSearch) {
        this.xmlns$openSearch = xmlns$openSearch;
    }

    /**
     * @return The xmlns$batch
     */
    public String getXmlns$batch() {
        return xmlns$batch;
    }

    /**
     * @param xmlns$batch The xmlns$batch
     */
    public void setXmlns$batch(String xmlns$batch) {
        this.xmlns$batch = xmlns$batch;
    }

    /**
     * @return The xmlns$gs
     */
    public String getXmlns$gs() {
        return xmlns$gs;
    }

    /**
     * @param xmlns$gs The xmlns$gs
     */
    public void setXmlns$gs(String xmlns$gs) {
        this.xmlns$gs = xmlns$gs;
    }

    /**
     * @return The id
     */
    public Id getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(Id id) {
        this.id = id;
    }

    /**
     * @return The updated
     */
    public Updated getUpdated() {
        return updated;
    }

    /**
     * @param updated The updated
     */
    public void setUpdated(Updated updated) {
        this.updated = updated;
    }

    /**
     * @return The category
     */
    public List<Category> getCategory() {
        return category;
    }

    /**
     * @param category The category
     */
    public void setCategory(List<Category> category) {
        this.category = category;
    }

    /**
     * @return The title
     */
    public Title getTitle() {
        return title;
    }

    /**
     * @param title The title
     */
    public void setTitle(Title title) {
        this.title = title;
    }

    /**
     * @return The link
     */
    public List<Link> getLink() {
        return link;
    }

    /**
     * @param link The link
     */
    public void setLink(List<Link> link) {
        this.link = link;
    }

    /**
     * @return The author
     */
    public List<Author> getAuthor() {
        return author;
    }

    /**
     * @param author The author
     */
    public void setAuthor(List<Author> author) {
        this.author = author;
    }

    /**
     * @return The openSearch$totalResults
     */
    public OpenSearch$totalResults getOpenSearch$totalResults() {
        return openSearch$totalResults;
    }

    /**
     * @param openSearch$totalResults The openSearch$totalResults
     */
    public void setOpenSearch$totalResults(OpenSearch$totalResults openSearch$totalResults) {
        this.openSearch$totalResults = openSearch$totalResults;
    }

    /**
     * @return The openSearch$startIndex
     */
    public OpenSearch$startIndex getOpenSearch$startIndex() {
        return openSearch$startIndex;
    }

    /**
     * @param openSearch$startIndex The openSearch$startIndex
     */
    public void setOpenSearch$startIndex(OpenSearch$startIndex openSearch$startIndex) {
        this.openSearch$startIndex = openSearch$startIndex;
    }

    /**
     * @return The entry
     */
    public List<Entry> getEntry() {
        return entry;
    }

    /**
     * @param entry The entry
     */
    public void setEntry(List<Entry> entry) {
        this.entry = entry;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
