package com.learn.bookService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;

import com.learn.bookService.persistence.model.Book;

import org.springframework.http.HttpStatus;

import io.restassured.RestAssured;
import io.restassured.response.Response;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpringBootBootstrapLiveTest {

    @LocalServerPort
    private int port;
    private String API_ROOT;

    @BeforeEach
    public void setUp() {
        API_ROOT = "http://localhost:" + port + "/api/books";
        RestAssured.port = port;
    }

//    private Book createRandomBook() {
//        final Book book = new Book();
//        book.setTitle(randomAlphabetic(10));
//        book.setAuthor(randomAlphabetic(15));
//        return book;
//    }
//
//    private String randomAlphabetic(int length) {
//    	 return RandomStringUtils.randomAlphabetic(length);
//    }
//
//
//	private String createBookAsUri(Book book) {
//        final Response response = RestAssured.given()
//          .contentType(MediaType.APPLICATION_JSON_VALUE)
//          .body(book)
//          .post(API_ROOT);
//        return API_ROOT + "/" + response.jsonPath().get("id");
//    }
//	
//	@Test
//	public void whenGetAllBooks_thenOK() {
//	    Response response = RestAssured.get(API_ROOT);
//	 
//	    assertEquals(HttpStatus.OK.value(), response.getStatusCode());
//	}
//
//	@Test
//	public void whenGetBooksByTitle_thenOK() {
//	    Book book = createRandomBook();
//	    createBookAsUri(book);
//	    Response response = RestAssured.get(
//	      API_ROOT + "/title/" + book.getTitle());
//	    
//	    assertEquals(HttpStatus.OK.value(), response.getStatusCode());
//	    assertTrue(response.as(List.class)
//	      .size() > 0);
//	}
//	@Test
//	public void whenGetCreatedBookById_thenOK() {
//	    Book book = createRandomBook();
//	    String location = createBookAsUri(book);
//	    Response response = RestAssured.get(location);
//	    
//	    assertEquals(HttpStatus.OK.value(), response.getStatusCode());
//	    assertEquals(book.getTitle(), response.jsonPath()
//	      .get("title"));
//	}
//
//	@Test
//	public void whenGetNotExistBookById_thenNotFound() {
//	    Response response = RestAssured.get(API_ROOT + "/" + randomNumeric(4));
//	    
//	    assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
//	}
//
//	private String randomNumeric(int length) {
//		// TODO Auto-generated method stub
//		return RandomStringUtils.randomNumeric(length);
//	}

}
