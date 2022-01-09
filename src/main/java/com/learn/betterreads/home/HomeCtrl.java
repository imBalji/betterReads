package com.learn.betterreads.home;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.learn.betterreads.user.BooksByUser;
import com.learn.betterreads.user.BooksByUserRepo;

@Controller
public class HomeCtrl {

	private final String COVER_IMAGE_ROOT = "http://covers.openlibrary.org/b/id/";

	@Autowired
	private BooksByUserRepo booksByUserRepo;

	@RequestMapping(method = RequestMethod.GET, path = "/")
	public String home(@AuthenticationPrincipal OAuth2User principal, Model model) {

		if (principal == null || principal.getAttribute("login") == null)
			return "index";

		Slice<BooksByUser> booksSlice = booksByUserRepo.findAllById(principal.getAttribute("login"),
				CassandraPageRequest.of(0, 10));
		List<BooksByUser> booksByUser = booksSlice.getContent();

		booksByUser = booksByUser.stream().distinct().map(book -> {
			String coverImageUrl = "/images/no_img.jpg";
			if (book.getCoverIds() != null && book.getCoverIds().size() > 0)
				coverImageUrl = COVER_IMAGE_ROOT + book.getCoverIds().get(0) + "-M.jpg";
			book.setCoverUrl(coverImageUrl);
			return book;
		}).collect(Collectors.toList());

		model.addAttribute("books", booksByUser);

		return "home";
	}
}