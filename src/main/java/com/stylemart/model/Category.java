package com.stylemart.model;

public class Category {
    private int id;
    private String name;
    private String slug;
    private String iconUrl;
    private Integer parentId;
    private boolean active;

    public Category() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
