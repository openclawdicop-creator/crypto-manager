package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.HistoricoCotacao;
import org.acme.repository.HistoricoCotacaoRepository;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import org.acme.resource.ApiResponse;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class HistoricoCotacaoService {

    @Inject
    HistoricoCotacaoRepository historicoCotacaoRepository;

    public List<HistoricoCotacao> listarTodos() {
        return historicoCotacaoRepository.listAll(Sort.descending("dataHoraConsulta"));
    }

    public ApiResponse<List<HistoricoCotacao>> listarPorParametrizacaoIdPaginado(Long parametrizacaoId, int page, int size) {
        List<HistoricoCotacao> content = historicoCotacaoRepository.findByParametrizacaoId(parametrizacaoId, page, size);
        long totalElements = historicoCotacaoRepository.countByParametrizacaoId(parametrizacaoId);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        ApiResponse.PageMetadata metadata = new ApiResponse.PageMetadata(page, size, totalElements, totalPages);
        return ApiResponse.successWithPagination(content, metadata);
    }

    public Optional<HistoricoCotacao> buscarPorId(Long id) {
        return historicoCotacaoRepository.findByIdOptional(id);
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
