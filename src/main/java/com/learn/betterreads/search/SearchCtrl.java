package com.learn.betterreads.search;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Controller
public class SearchCtrl {

	private final String COVER_IMAGE_ROOT = "http://covers.openlibrary.org/b/id/";

	private final WebClient webClient;

	public SearchCtrl(WebClient.Builder webClientBuilder) {
		super();
		this.webClient = webClientBuilder
				.exchangeStrategies(ExchangeStrategies.builder()
						.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)).build())
				.baseUrl("http://openlibrary.org/search.json").build();
	}

	@RequestMapping(method = RequestMethod.GET, path = "/search")
	public String getSearchResults(@RequestParam String query, Model model) {

		Mono<Search> temp = this.webClient.get().uri("?q={query}", query).retrieve().bodyToMono(Search.class);
		Search result = temp.block();
		List<SearchBook> books = result.getDocs().stream().limit(10).map(book -> {
			book.setKey(book.getKey().replace("/works/", ""));
			if (StringUtils.hasText(book.getCover_i()))
				book.setCover_i(COVER_IMAGE_ROOT + book.getCover_i() + "-M.jpg");
			else
				book.setCover_i("/images/no_img.jpg");
			return book;
		}).collect(Collectors.toList());
		model.addAttribute("searchResults", books);
		return "search";
	}
}