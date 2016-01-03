package serverws;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import server.HotelServer;
import server.DataTypes.HotelProfile;
import server.impl.HotelServerImpl;


@SpringBootApplication
public class Application {

	@Bean
	public HotelServer hotelServer() {
	    HotelServer server = HotelServerImpl.createServer("config.properties.Gordon");
	    return server;
	}
	
    public static void main(String[] args) {
    	ApplicationContext ctx = SpringApplication.run(Application.class, args);
        
        System.out.println("Let's inspect the beans provided by Spring Boot:");

        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            System.out.println(beanName);
        }
    }
}