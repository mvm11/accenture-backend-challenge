package co.com.bancolombia.api;

import co.com.bancolombia.api.franchise.FranchiseHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {

    @Bean
    public RouterFunction<ServerResponse> routerFunction(
            FranchiseHandler franchiseHandler,
            @Value("${api.paths.franchises}") String franchisesPath) {
        return route(POST(franchisesPath), franchiseHandler::createFranchise);
    }
}