package com.learn.bookService.persistence.repo;

import java.util.List;

// A generic class to represent a page of results for cursor-based pagination
// T is the type of items in the page
// items: the list of items in the current page
// nextCursor: the cursor value to be used for fetching the next page
// hasNext: a boolean indicating whether there are more pages available after the current one
// This class is used to encapsulate the results of a paginated query, allowing the client to easily access
// the items and determine if there are more pages to fetch
public class CursorPage<T> {

    private final List<T> items;
    private final Long nextCursor;
    private final Long prevCursor;
    private final boolean hasNext;
    private final boolean hasPrev;

    public CursorPage(List<T> items, Long nextCursor, Long prevCursor, boolean hasNext, boolean hasPrev) {
        this.items = items;
        this.nextCursor = nextCursor;
        this.prevCursor = prevCursor;
        this.hasNext = hasNext;
        this.hasPrev = hasPrev;
    }

    public List<T> getItems() { return items; }
    public Long getNextCursor() { return nextCursor; }
    public Long getPrevCursor() { return prevCursor; }
    public boolean isHasNext() { return hasNext; }
    public boolean isHasPrev() { return hasPrev; }
}

