package br.edu.atitus.currencyapi.config;

import br.edu.atitus.currencyapi.clients.BcbClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class BcbClientConfig {

    @Bean
    public BcbClient bcbClient() {
        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(2000);
        requestFactory.setReadTimeout(5000);

        RestClient restClient = RestClient.builder()
                .baseUrl("https://olinda.bcb.gov.br")
                .requestFactory(requestFactory)
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);

        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(BcbClient.class);
    }
}