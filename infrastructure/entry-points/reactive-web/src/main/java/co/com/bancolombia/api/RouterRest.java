package co.com.bancolombia.api;

import co.com.bancolombia.api.branch.BranchHandler;
import co.com.bancolombia.api.franchise.FranchiseHandler;
import co.com.bancolombia.api.product.ProductHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.PATCH;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {

    private static final String ID_SEGMENT = "/{id}";

    @Bean
    public RouterFunction<ServerResponse> routerFunction(
            FranchiseHandler franchiseHandler,
            BranchHandler branchHandler,
            ProductHandler productHandler,
            @Value("${api.paths.franchises}") String franchisesPath) {

        String franchisePath  = franchisesPath + ID_SEGMENT;
        String branchesPath   = franchisesPath + "/{franchiseId}/branches";
        String branchPath     = branchesPath   + ID_SEGMENT;
        String productsPath   = branchesPath   + "/{branchId}/products";
        String productPath    = productsPath   + ID_SEGMENT;

        return route(POST(franchisesPath),          franchiseHandler::createFranchise)
                .andRoute(GET(franchisePath),        franchiseHandler::getFranchiseById)
                .andRoute(PATCH(franchisePath),      franchiseHandler::updateFranchiseName)
                .andRoute(POST(branchesPath),        branchHandler::createBranch)
                .andRoute(GET(branchPath),           branchHandler::getBranchById)
                .andRoute(PATCH(branchPath),         branchHandler::updateBranchName)
                .andRoute(POST(productsPath),        productHandler::createProduct)
                .andRoute(GET(productPath),          productHandler::getProductById)
                .andRoute(PATCH(productPath + "/name"),  productHandler::updateProductName)
                .andRoute(PATCH(productPath + "/stock"), productHandler::updateProductStock)
                .andRoute(DELETE(productPath),       productHandler::deleteProduct);
    }
}
