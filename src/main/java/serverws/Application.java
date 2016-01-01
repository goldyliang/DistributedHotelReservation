package serverws;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import HotelServer.HotelServer;
import HotelServerInterface.IHotelServer;
import common.HotelServerTypes.HotelProfile;


@SpringBootApplication
public class Application {

	@Bean
	public IHotelServer hotelServer() {
	    IHotelServer server = HotelServer.createServer("config.properties.Gordon");
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