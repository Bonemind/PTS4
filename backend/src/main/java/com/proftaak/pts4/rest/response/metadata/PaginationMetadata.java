package com.proftaak.pts4.rest.response.metadata;

/**
 * Created by Michon on 22-6-2015.
 */
public class PaginationMetadata {
    /**
     * The current page number
     */
    private int page;

    /**
     * The total number of pages
     */
    private int pages;

    /**
     * The current effective maximum number of results per page
     */
    private int limit;

    /**
     * The total number of results
     */
    private int count;

    public int getPage() {
        return this.page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPages() {
        return this.pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public int getLimit() {
        return this.limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
