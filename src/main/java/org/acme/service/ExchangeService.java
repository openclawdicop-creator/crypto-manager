package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.Exchange;
import org.acme.repository.ExchangeRepository;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ExchangeService {

    @Inject
    ExchangeRepository exchangeRepository;

    public List<Exchange> listarTodos() {
        return exchangeRepository.listAll();
    }

    public Optional<Exchange> buscarPorId(Long id) {
        return exchangeRepository.findByIdOptional(id);
    }

    @Transactional
    public Exchange criar(Exchange exchange) {
        exchangeRepository.persist(exchange);
        return exchange;
    }

    @Transactional
    public Exchange atualizar(Long id, Exchange exchangeAtualizado) {
        Exchange exchange = exchangeRepository.findById(id);
        if (exchange == null) {
            throw new IllegalArgumentException("Exchange não encontrada");
        }
        exchange.nome = exchangeAtualizado.nome;
        exchange.descricao = exchangeAtualizado.descricao;
        exchange.tipo = exchangeAtualizado.tipo;
        exchange.tipoApi = exchangeAtualizado.tipoApi;
        exchange.tokenApi = exchangeAtualizado.tokenApi;
        exchange.urlApi = exchangeAtualizado.urlApi;
        exchange.logHabilitado = exchangeAtualizado.logHabilitado;
        exchangeRepository.persist(exchange);
        return exchange;
    }

    @Transactional
    public void excluir(Long id) {
        exchangeRepository.deleteById(id);
    }
}
