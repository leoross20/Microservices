package br.edu.atitus.currencyapi.servicesimpl;

import br.edu.atitus.currencyapi.dtos.CurrencyResponse;
import br.edu.atitus.currencyapi.repositories.CurrencyRepository;
import br.edu.atitus.currencyapi.services.CurrencyService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CurrencyServiceJpa implements CurrencyService {

    private final CurrencyRepository repository;
    private final BcbResilienceService bcbResilienceService;

    @Value("${server.port:8080}")
    private String serverPort;

    public CurrencyServiceJpa(
            CurrencyRepository repository,
            BcbResilienceService bcbResilienceService) {
        this.repository = repository;
        this.bcbResilienceService = bcbResilienceService;
    }

    @Override
    public CurrencyResponse findBySourceCurrencyAndTargetCurrency(
            String sourceCurrency, String targetCurrency) throws Exception {

        String source = sourceCurrency.toUpperCase();
        String target = targetCurrency.toUpperCase();

        if ("USD".equals(source) && "BRL".equals(target)) {
            try {
                Double cotacaoBcb =
                        bcbResilienceService.consultarCotacao(source);

                if (cotacaoBcb != null) {
                    String environment = "Currency API running in port "
                            + serverPort + " - Banco Central PTAX";

                    return new CurrencyResponse(
                            source, target, cotacaoBcb, environment);
                }
            } catch (Exception e) {
                System.err.println(
                        "ERRO AO CONSULTAR O BANCO CENTRAL:");
                e.printStackTrace();
            }
        }

        var currency = repository
                .findBySourceCurrencyAndTargetCurrency(source, target)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Currency não encontrado"));

        String environment =
                "Currency API running in port " + serverPort;

        return new CurrencyResponse(
                currency.getSourceCurrency(),
                currency.getTargetCurrency(),
                currency.getConversionRate(),
                environment
        );
    }
}