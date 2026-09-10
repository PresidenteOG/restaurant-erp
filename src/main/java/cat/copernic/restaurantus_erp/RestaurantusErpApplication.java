package cat.copernic.restaurantus_erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RestaurantusErpApplication {
	public static void main(String[] args) {
		SpringApplication.run(RestaurantusErpApplication.class, args);
	}
}
