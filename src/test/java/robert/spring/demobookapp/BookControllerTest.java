package robert.spring.demobookapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(BookController.class)
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @Captor
    private ArgumentCaptor<BookRequest> bookRequestArgumentCaptor;

    @Test
    public void postingANewBookShouldCreateANewBookInTheDatabase() throws Exception {

        BookRequest bookRequest = new BookRequest();
        bookRequest.setAuthor("Duke");
        bookRequest.setTitle("Java 8");
        bookRequest.setIsbn("1232");

        when(bookService.createNewBook(bookRequestArgumentCaptor.capture())).thenReturn(1L);

        this.mockMvc.perform(post("/api/books")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", "http://localhost/api/books/1"));

        assertThat(bookRequestArgumentCaptor.getValue().getAuthor(), is("Duke"));
        assertThat(bookRequestArgumentCaptor.getValue().getIsbn(), is("1232"));
        assertThat(bookRequestArgumentCaptor.getValue().getTitle(), is("Java 8"));
    }

    @Test
    public void allBooksEndpointShouldReturnTwoBooks() throws Exception {

        when(bookService.getAllBooks()).thenReturn(Stream.of(createBook(1L, "Java 8", "Duke", "1232"),
                createBook(2L, "Java EE 8", "Duke", "42")).collect(Collectors.toList()));

        this.mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Java 8")))
                .andExpect(jsonPath("$[0].author", is("Duke")))
                .andExpect(jsonPath("$[0].isbn", is("1232")))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    public void getBookWithIdOneShouldReturnABook() throws Exception {

        when(bookService.getBookById(1L)).thenReturn(createBook(1L, "Java 8", "Duke", "1232"));

        this.mockMvc
                .perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.title", is("Java 8")))
                .andExpect(jsonPath("$.author", is("Duke")))
                .andExpect(jsonPath("$.isbn", is("1232")))
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    public void getBookWithUnknownIdShouldReturn404() throws Exception {

        when(bookService.getBookById(12L)).thenThrow(new BookNotFoundException("Book with id '12' not found"));

        this.mockMvc
                .perform(get("/api/books/12"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateBookWithKnownIdShouldUpdateTheBook() throws Exception{

        BookRequest bookRequest = new BookRequest();
        bookRequest.setAuthor("Duke");
        bookRequest.setTitle("Java 88");
        bookRequest.setIsbn("1232");

        when(bookService.updateBook(eq(1L), bookRequestArgumentCaptor.capture()))
                .thenReturn(createBook(1L, "Java 88", "Duke", "1232"));

        this.mockMvc
                .perform(put("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.title", is("Java 88")))
                .andExpect(jsonPath("$.author", is("Duke")))
                .andExpect(jsonPath("$.isbn", is("1232")))
                .andExpect(jsonPath("$.id", is(1)));

        assertThat(bookRequestArgumentCaptor.getValue().getAuthor(), is("Duke"));
        assertThat(bookRequestArgumentCaptor.getValue().getIsbn(), is("1232"));
        assertThat(bookRequestArgumentCaptor.getValue().getTitle(), is("Java 88"));
    }

    @Test
    public void updateBookWithUnknownIdShouldReturn404() throws Exception {

        BookRequest bookRequest = new BookRequest();
        bookRequest.setAuthor("Duke");
        bookRequest.setTitle("Java 88");
        bookRequest.setIsbn("1232");

        when(bookService.updateBook(eq(42L), bookRequestArgumentCaptor.capture()))
                .thenThrow(new BookNotFoundException("The book with id '42' was not found"));

        this.mockMvc
                .perform(put("/api/books/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isNotFound());
    }

    private Book createBook(long id, String title, String author, String isbn) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setId(id);
        book.setIsbn(isbn);
        return book;
    }
}
