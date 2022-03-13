package com.example.nijiScraping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

@SpringBootApplication
public class NijiScrapingApplication {

	@Autowired
	private Scraping scraping;

	public static void main(String[] args) throws IOException{
		ConfigurableApplicationContext ctx  = SpringApplication.run(NijiScrapingApplication.class, args);
		NijiScrapingApplication app = ctx.getBean(NijiScrapingApplication.class);
		app.execStartup();
	}

	public void execStartup() throws IOException{
		scraping.saveMemberInfo();
	}
}
