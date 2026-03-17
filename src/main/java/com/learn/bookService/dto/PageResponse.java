package com.learn.bookService.dto;

import java.util.List;

// This is a generic record class that can be used to represent paginated responses
// for any type of content.
// The record includes the following fields:
// - content: A list of items of type T that represents the current page's content.
// - page: An integer representing the current page number (0-based index).
// - size: An integer representing the number of items per page.
// - totalElements: A long representing the total number of items across all pages.
// - totalPages: An integer representing the total number of pages available based on the total elements and page size.
// By using a record, we get an immutable data structure with automatically generated methods
// like equals(), hashCode(), and toString(), which simplifies the code and reduces boilerplate.
// equals() and hashCode() automatically include all record fields, in order.
// toString() will return a string representation of the record, 
// including all fields and their values in a readable format.

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}



//PageResponse<String> resp = new PageResponse<>(List.of("A", "B"), 1, 10, 50, 5);
//System.out.println(resp);
//// Output: PageResponse[content=[A, B], page=1, size=10, totalElements=50, totalPages=5]