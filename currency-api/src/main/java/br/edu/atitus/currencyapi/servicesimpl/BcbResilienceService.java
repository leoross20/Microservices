package br.edu.atitus.currencyapi.servicesimpl;

import br.edu.atitus.currencyapi.clients.BcbClient;
import br.edu.atitus.currencyapi.dtos.BcbCotacao;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.util.Comparator;

@Service
public class BcbResilienceService {

    private final BcbClient bcbClient;

    private static final String DATA_COTACAO = "10-10-2025";

    public BcbResilienceService(BcbClient bcbClient) {
        this.bcbClient = bcbClient;
    }

    @Cacheable(
            cacheNames = "cotacoesBcb",
            key = "#moeda + '-' + '10-10-2025'",
            unless = "#result == null"
    )
    @Retry(name = "bcb-api")
    @CircuitBreaker(name = "bcb-api")
    public Double consultarCotacao(String moeda) {
        System.out.println("CONSULTANDO BANCO CENTRAL: " + moeda);

        var resposta = bcbClient.getCotacao(moeda, DATA_COTACAO);

        if (resposta == null
                || resposta.value() == null
                || resposta.value().isEmpty()) {
            return null;
        }

        return resposta.value().stream()
                .filter(cotacao ->
                        "Fechamento PTAX".equalsIgnoreCase(
                                cotacao.tipoBoletim()))
                .max(Comparator.comparing(BcbCotacao::dataHoraCotacao))
                .map(BcbCotacao::cotacaoVenda)
                .orElse(null);
    }
}