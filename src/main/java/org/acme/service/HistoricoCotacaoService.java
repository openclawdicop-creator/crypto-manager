package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.HistoricoCotacao;
import org.acme.repository.HistoricoCotacaoRepository;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class HistoricoCotacaoService {

    @Inject
    HistoricoCotacaoRepository historicoCotacaoRepository;

    public List<HistoricoCotacao> listarTodos() {
        return historicoCotacaoRepository.listAll();
    }

    public Optional<HistoricoCotacao> buscarPorId(Long id) {
        return historicoCotacaoRepository.findByIdOptional(id);
    }

    public List<HistoricoCotacao> listarPorParametrizacaoId(Long parametrizacaoId) {
        return historicoCotacaoRepository.findByParametrizacaoId(parametrizacaoId);
    }

    @Transactional
    public HistoricoCotacao criar(HistoricoCotacao historicoCotacao) {
        historicoCotacaoRepository.persist(historicoCotacao);
        return historicoCotacao;
    }

    @Transactional
    public void excluir(Long id) {
        historicoCotacaoRepository.deleteById(id);
    }
}
