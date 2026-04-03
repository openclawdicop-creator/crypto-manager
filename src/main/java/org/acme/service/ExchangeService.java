package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.CategoriaExchange;
import org.acme.entity.Exchange;
import org.acme.repository.ExchangeRepository;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ExchangeService {

    private static final int PROFUNDIDADE_PADRAO_LIVRO_OFERTAS = 10;

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
        exchange.categoria = normalizarCategoria(exchange.categoria);
        exchange.profundidadeLivroOfertas = normalizarProfundidadeLivroOfertas(exchange.profundidadeLivroOfertas);
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
        exchange.categoria = normalizarCategoria(exchangeAtualizado.categoria);
        exchange.tokenApi = exchangeAtualizado.tokenApi;
        exchange.urlApi = exchangeAtualizado.urlApi;
        exchange.profundidadeLivroOfertas = normalizarProfundidadeLivroOfertas(exchangeAtualizado.profundidadeLivroOfertas);
        exchange.usarProxy = exchangeAtualizado.usarProxy;
        exchange.logHabilitado = exchangeAtualizado.logHabilitado;
        exchangeRepository.persist(exchange);
        return exchange;
    }

    @Transactional
    public void excluir(Long id) {
        exchangeRepository.deleteById(id);
    }

    private CategoriaExchange normalizarCategoria(CategoriaExchange categoria) {
        return categoria != null ? categoria : CategoriaExchange.SPOT;
    }

    private Integer normalizarProfundidadeLivroOfertas(Integer profundidadeLivroOfertas) {
        if (profundidadeLivroOfertas == null) {
            return PROFUNDIDADE_PADRAO_LIVRO_OFERTAS;
        }

        if (profundidadeLivroOfertas <= 0) {
            throw new IllegalArgumentException("Profundidade do livro de ofertas deve ser maior que zero.");
        }

        return profundidadeLivroOfertas;
    }
}
