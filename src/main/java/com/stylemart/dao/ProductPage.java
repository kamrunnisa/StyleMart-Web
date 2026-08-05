package com.stylemart.dao;

import com.stylemart.model.Product;

import java.util.List;

public class ProductPage {
    private final List<Product> products;
    private final int totalCount;
    private final int page;
    private final int pageSize;

    public ProductPage(List<Product> products, int totalCount, int page, int pageSize) {
        this.products = products;
        this.totalCount = totalCount;
        this.page = page;
        this.pageSize = pageSize;
    }

    public List<Product> getProducts() { return products; }
    public int getTotalCount() { return totalCount; }
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
    public int getTotalPages() { return (int) Math.ceil(totalCount / (double) pageSize); }
    public boolean isHasNext() { return page < getTotalPages(); }
    public boolean isHasPrev() { return page > 1; }
}
