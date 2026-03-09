package com.learn.bookService.client;

//In book-service, create a DTO for the response

public class BookPriceResponse {
 private Long bookId;
 public Long getBookId() {
	return bookId;
}
 public void setBookId(Long bookId) {
	this.bookId = bookId;
 }
 public double getPrice() {
	return price;
 }
 public void setPrice(double price) {
	this.price = price;
 }
 public String getCurrency() {
	return currency;
 }
 public void setCurrency(String currency) {
	this.currency = currency;
 }
 private double price;
 private String currency;
}