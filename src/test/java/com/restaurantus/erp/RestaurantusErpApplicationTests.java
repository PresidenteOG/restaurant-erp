package com.restaurantus.erp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// El perfil "demo" arranca el contexto completo sin MySQL/MongoDB/Telegram
// (H2 en memoria + bundle de i18n), asi que este smoke test corre sin infra.
@SpringBootTest
@ActiveProfiles("demo")
class RestaurantusErpApplicationTests {

	@Test
	void contextLoads() {
	}

}
