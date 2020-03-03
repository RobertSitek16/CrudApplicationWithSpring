package robert.spring.demobookapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public Long createNewBook(BookRequest bookRequest) {
        Book book = new Book();
        book.setAuthor(bookRequest.getAuthor());
        book.setIsbn(bookRequest.getIsbn());
        book.setTitle(bookRequest.getTitle());

        book = bookRepository.save(book);

        return book.getId();
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(Long id) {
        Optional<Book> requestdBook = bookRepository.findById(id);

        if (!requestdBook.isPresent()){
            throw new BookNotFoundException(String.format("Book with id: '%s' not found", id));
        }

        return requestdBook.get();
    }

    @Transactional
    public Book updateBook(Long id, BookRequest bookToUpdateRequest) {

        Optional<Book> bookFromDatabase = bookRepository.findById(id);

        if (!bookFromDatabase.isPresent()){
            throw new BookNotFoundException(String.format("Book with id: '%s' not found", id));
        }

        Book bookToUpdate = bookFromDatabase.get();

        bookToUpdate.setAuthor(bookToUpdateRequest.getAuthor());
        bookToUpdate.setIsbn(bookToUpdateRequest.getIsbn());
        bookToUpdate.setTitle(bookToUpdateRequest.getTitle());

        return bookToUpdate;
    }

    public void deleteBookById(Long id) {
        bookRepository.deleteById(id);
    }
}
