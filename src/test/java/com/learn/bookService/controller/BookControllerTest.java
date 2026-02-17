package com.learn.bookService.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.learn.bookService.dto.BookDTO;
import com.learn.bookService.persistence.service.BookOperations;

/**
 * @WebMvcTest is a specialized test annotation for testing Spring MVC controllers. 
 * It sets up a minimal Spring application context that includes only the components
 * needed for testing the web layer, such as controllers, filters, and related configurations.
 * This allows you to test your controller's behavior in isolation from the rest of the application.
 * 
 * @AutoConfigureMockMvc(addFilters = false) is used to configure MockMvc for testing the web layer.
 */

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerTest {
    
    @Autowired
    private MockMvc mockMvc;	
    
    @MockBean
    private BookOperations bookOps;

    @Test
    void testFindAll() throws Exception {
        BookDTO book = new BookDTO();
        book.setAuthor("Test Test");
        book.setPrice(BigDecimal.valueOf(100.0));
        book.setTitle("Test");
        when(bookOps.findById(3L)).thenReturn( book);
        
            // controller is mapped to /api/v1/books
            mockMvc.perform(get("/api/v1/books/3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Test"))
            .andExpect(jsonPath("$.author").value("Test Test"))
            .andExpect(jsonPath("$.price").value(100.0));

        
    }

}
