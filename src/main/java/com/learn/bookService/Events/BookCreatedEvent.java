package com.learn.bookService.Events;

import java.math.BigDecimal;
import java.util.Date;

public class BookCreatedEvent {

    private Long id;
    private String title;
    private String author;
    private BigDecimal price;
    private TestKafkaObject createDate;
    
 // No-args constructor (required for deserialization)
    public BookCreatedEvent() {}
    
	public BookCreatedEvent(Long id, String title, String author, BigDecimal bigDecimal) {
		this.author = author;
		this.id = id;
		this.price = bigDecimal;
		this.title = title;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	
	public TestKafkaObject getCreateDate() {
		return createDate;
	}

	public void setCreateDate(TestKafkaObject createDate) {
		this.createDate = createDate;
	}
	
	@Override
	public String toString() {
		return this.getId() + ":" +  this.getTitle() + ":" + this.getAuthor() + ":" +
				this.getPrice();
	}
}