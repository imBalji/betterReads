package com.learn.betterreads.book;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.learn.betterreads.userbooks.UserBooks;
import com.learn.betterreads.userbooks.UserBooksPrimaryKey;
import com.learn.betterreads.userbooks.UserBooksRepo;

@Controller
public class BookCtrl {

	private final String COVER_IMAGE_ROOT = "http://covers.openlibrary.org/b/id/";

	@Autowired
	private BookRepo bookRepo;

	@Autowired
	private UserBooksRepo userBooksRepo;

	@RequestMapping(path = "/books/{bookId}")
	public String getBook(@PathVariable String bookId, Model model, @AuthenticationPrincipal OAuth2User principal) {
		Optional<Book> optBook = bookRepo.findById(bookId);

		if (optBook.isPresent()) {
			Book book = optBook.get();
			String coverImageUrl = "/images/no_img.jpg";
			if (book.getCoverIds() != null && book.getCoverIds().size() > 0)
				coverImageUrl = COVER_IMAGE_ROOT + book.getCoverIds().get(0) + "-L.jpg";
			model.addAttribute("coverImage", coverImageUrl);
			model.addAttribute("book", book);

			if (principal != null && principal.getAttribute("login") != null) {
				model.addAttribute("loginId", principal.getAttribute("login"));
				
				UserBooksPrimaryKey booksPrimaryKey = new UserBooksPrimaryKey();
				booksPrimaryKey.setBookId(bookId);
				booksPrimaryKey.setUserId(principal.getAttribute("login"));
				
				Optional<UserBooks> optUserBooks = userBooksRepo.findById(booksPrimaryKey);
				if(optUserBooks.isPresent())
					model.addAttribute("userBooks", optUserBooks.get());
				else
					model.addAttribute("userBooks", new UserBooks());
			}
			return "book";
		}
		return "book-not-found";
	}
}