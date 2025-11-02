
package com.interview.dto;

import java.util.List;

public class AutoPartQueryRequest {

    private Filter filter;
    private Pagination pagination;
    private Aggregation aggregation;

    public static class Filter {
        private String category;
        private String manufacturer;
        private Double minPrice;
        private Double maxPrice;
        private Integer minStock;
        private Integer maxStock;

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getManufacturer() { return manufacturer; }
        public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

        public Double getMinPrice() { return minPrice; }
        public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }

        public Double getMaxPrice() { return maxPrice; }
        public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }

        public Integer getMinStock() { return minStock; }
        public void setMinStock(Integer minStock) { this.minStock = minStock; }

        public Integer getMaxStock() { return maxStock; }
        public void setMaxStock(Integer maxStock) { this.maxStock = maxStock; }
    }

    public static class Pagination {
        private int page = 0;
        private int size = 10;
        private String sortBy = "id";
        private String direction = "asc";

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }

        public String getSortBy() { return sortBy; }
        public void setSortBy(String sortBy) { this.sortBy = sortBy; }

        public String getDirection() { return direction; }
        public void setDirection(String direction) { this.direction = direction; }
    }

    public static class Aggregation {
        private List<String> fields; // e.g., ["price", "stockQuantity"]
        private String function;     // e.g., "avg", "sum", "count"

        public List<String> getFields() { return fields; }
        public void setFields(List<String> fields) { this.fields = fields; }

        public String getFunction() { return function; }
        public void setFunction(String function) { this.function = function; }
    }

    public Filter getFilter() { return filter; }
    public void setFilter(Filter filter) { this.filter = filter; }

    public Pagination getPagination() { return pagination; }
    public void setPagination(Pagination pagination) { this.pagination = pagination; }

    public Aggregation getAggregation() { return aggregation; }
    public void setAggregation(Aggregation aggregation) { this.aggregation = aggregation; }
}
