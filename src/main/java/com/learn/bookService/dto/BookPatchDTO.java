package com.learn.bookService.dto;

import java.math.BigDecimal;


// Validation is conditional since it is a patch
public class BookPatchDTO {
	
    private String author;

    private String title;

    private BigDecimal price;

    public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}
}
