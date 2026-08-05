package com.stylemart.dao;

import java.math.BigDecimal;

/**
 * Plain criteria holder built by ProductListServlet from request parameters
 * and consumed by ProductDAO.search(). Keeping this out of the model package
 * because it isn't a persisted entity -- it's a query shape.
 */
public class ProductFilter {

    private Integer categoryId;
    private String keyword;
    private String flag;          // trending | new_arrival | best_seller | featured | flash_sale
    private String brand;
    private String size;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sort;          // popularity | price_low | price_high | newest | rating
    private int page = 1;
    private int pageSize = 12;

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getFlag() { return flag; }
    public void setFlag(String flag) { this.flag = flag; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }

    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }

    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = Math.max(page, 1); }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    public int getOffset() { return (page - 1) * pageSize; }
}
