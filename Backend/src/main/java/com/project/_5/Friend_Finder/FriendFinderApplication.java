package com.project._5.Friend_Finder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO) // for a warning i got
public class FriendFinderApplication {

	public static void main(String[] args) {
		SpringApplication.run(FriendFinderApplication.class, args);
	}

}
