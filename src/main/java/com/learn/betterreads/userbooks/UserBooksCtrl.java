package com.learn.betterreads.userbooks;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.learn.betterreads.book.Book;
import com.learn.betterreads.book.BookRepo;
import com.learn.betterreads.user.BooksByUser;
import com.learn.betterreads.user.BooksByUserRepo;

@Controller
public class UserBooksCtrl {

	@Autowired
	private UserBooksRepo userBooksRepository;
	
	@Autowired
	private BookRepo bookRepo;
	
	@Autowired
	private BooksByUserRepo booksByUserRepo;

	@RequestMapping(method = RequestMethod.POST, path = "/addUserBook")
	public ModelAndView addBookForUser(@RequestBody MultiValueMap<String, String> formData,
			@AuthenticationPrincipal OAuth2User principal) {

		if (principal == null || principal.getAttribute("login") == null)
			return null;

		UserBooksPrimaryKey key = new UserBooksPrimaryKey();
		key.setBookId(formData.getFirst("bookId"));
		key.setUserId(principal.getAttribute("login"));
		
		Optional<Book> optionalBook = bookRepo.findById(formData.getFirst("bookId"));
        if (!optionalBook.isPresent()) {
            return new ModelAndView("redirect:/");
        }
        Book book = optionalBook.get();

		UserBooks userBooks = new UserBooks();
		userBooks.setKey(key);
		userBooks.setStartedDate(LocalDate.parse(formData.getFirst("startDate")));
		userBooks.setCompletedDate(LocalDate.parse(formData.getFirst("completedDate")));
		userBooks.setReadingStatus(formData.getFirst("readingStatus"));
		userBooks.setRating(Integer.parseInt(formData.getFirst("rating")));
		userBooksRepository.save(userBooks);
		
		BooksByUser booksByUser = new BooksByUser();
        booksByUser.setId(principal.getAttribute("login"));
        booksByUser.setBookId(formData.getFirst("bookId"));
        booksByUser.setBookName(book.getName());
        booksByUser.setCoverIds(book.getCoverIds());
        booksByUser.setAuthorNames(book.getAuthorNames());
        booksByUser.setReadingStatus(formData.getFirst("readingStatus"));
        booksByUser.setRating(Integer.parseInt(formData.getFirst("rating")));
        booksByUserRepo.save(booksByUser);

		return new ModelAndView("redirect:/books/" + key.getBookId());
	}
}