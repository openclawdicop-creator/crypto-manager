package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.ParametrizacaoConsultaPreco;
import org.acme.repository.ParametrizacaoConsultaPrecoRepository;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ParametrizacaoConsultaPrecoService {

    @Inject
    ParametrizacaoConsultaPrecoRepository parametrizacaoConsultaPrecoRepository;

    public List<ParametrizacaoConsultaPreco> listarTodos() {
        return parametrizacaoConsultaPrecoRepository.listAll();
    }

    public Optional<ParametrizacaoConsultaPreco> buscarPorId(Long id) {
        return parametrizacaoConsultaPrecoRepository.findByIdOptional(id);
    }

    public List<ParametrizacaoConsultaPreco> listarPorAtiva(boolean ativa) {
        return parametrizacaoConsultaPrecoRepository.findByAtiva(ativa);
    }

    @Transactional
    public ParametrizacaoConsultaPreco criar(ParametrizacaoConsultaPreco parametrizacao) {
        parametrizacaoConsultaPrecoRepository.persist(parametrizacao);
        return parametrizacao;
    }

    @Transactional
    public ParametrizacaoConsultaPreco atualizar(Long id, ParametrizacaoConsultaPreco parametrizacaoAtualizada) {
        ParametrizacaoConsultaPreco parametrizacao = parametrizacaoConsultaPrecoRepository.findById(id);
        if (parametrizacao == null) {
            throw new IllegalArgumentException("Parametrização não encontrada");
        }
        parametrizacao.exchange = parametrizacaoAtualizada.exchange;
        parametrizacao.rede = parametrizacaoAtualizada.rede;
        parametrizacao.ativoDesejado = parametrizacaoAtualizada.ativoDesejado;
        parametrizacao.ativoPagamento = parametrizacaoAtualizada.ativoPagamento;
        parametrizacao.quantidadePagamento = parametrizacaoAtualizada.quantidadePagamento;
        parametrizacao.identificadorNegociacao = parametrizacaoAtualizada.identificadorNegociacao;
        parametrizacao.ativa = parametrizacaoAtualizada.ativa;
        parametrizacao.logHabilitado = parametrizacaoAtualizada.logHabilitado;
        parametrizacaoConsultaPrecoRepository.persist(parametrizacao);
        return parametrizacao;
    }

    @Transactional
    public void excluir(Long id) {
        parametrizacaoConsultaPrecoRepository.deleteById(id);
    }

    @Transactional
    public List<ParametrizacaoConsultaPreco> ativarTodas() {
        List<ParametrizacaoConsultaPreco> parametrizacoes = listarTodos();
        for (ParametrizacaoConsultaPreco p : parametrizacoes) {
            p.ativa = true;
            parametrizacaoConsultaPrecoRepository.persist(p);
        }
        return parametrizacoes;
    }

    @Transactional
    public List<ParametrizacaoConsultaPreco> desativarTodas() {
        List<ParametrizacaoConsultaPreco> parametrizacoes = listarTodos();
        for (ParametrizacaoConsultaPreco p : parametrizacoes) {
            p.ativa = false;
            parametrizacaoConsultaPrecoRepository.persist(p);
        }
        return parametrizacoes;
    }
}
