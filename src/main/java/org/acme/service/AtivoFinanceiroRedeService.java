package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.AtivoFinanceiro;
import org.acme.entity.AtivoFinanceiroRede;
import org.acme.entity.Rede;
import org.acme.repository.AtivoFinanceiroRedeRepository;
import org.acme.repository.AtivoFinanceiroRepository;
import org.acme.repository.RedeRepository;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AtivoFinanceiroRedeService {

    @Inject
    AtivoFinanceiroRedeRepository ativoFinanceiroRedeRepository;

    @Inject
    AtivoFinanceiroRepository ativoFinanceiroRepository;

    @Inject
    RedeRepository redeRepository;

    public List<AtivoFinanceiroRede> listarTodos() {
        return ativoFinanceiroRedeRepository.listAllOrdered();
    }

    public Optional<AtivoFinanceiroRede> buscarPorId(Long id) {
        return ativoFinanceiroRedeRepository.findByIdOptional(id);
    }

    @Transactional
    public AtivoFinanceiroRede criar(AtivoFinanceiroRede ativoFinanceiroRede) {
        validarCadastro(ativoFinanceiroRede);

        ativoFinanceiroRede.identificador = ativoFinanceiroRede.identificador.trim();
        ativoFinanceiroRede.quantidadeCasasDecimais = normalizarQuantidadeCasasDecimais(ativoFinanceiroRede.quantidadeCasasDecimais);
        ativoFinanceiroRede.ativoFinanceiro = buscarAtivoObrigatorio(ativoFinanceiroRede.ativoFinanceiro);
        ativoFinanceiroRede.rede = buscarRedeObrigatoria(ativoFinanceiroRede.rede);

        if (ativoFinanceiroRedeRepository.existsByAtivoAndRede(ativoFinanceiroRede.ativoFinanceiro.id, ativoFinanceiroRede.rede.id)) {
            throw new IllegalArgumentException("Ja existe uma configuracao para o ativo e rede informados");
        }

        ativoFinanceiroRedeRepository.persist(ativoFinanceiroRede);
        return ativoFinanceiroRede;
    }

    @Transactional
    public AtivoFinanceiroRede atualizar(Long id, AtivoFinanceiroRede ativoFinanceiroRedeAtualizado) {
        AtivoFinanceiroRede ativoFinanceiroRede = ativoFinanceiroRedeRepository.findById(id);
        if (ativoFinanceiroRede == null) {
            throw new IllegalArgumentException("Configuracao ativo rede nao encontrada");
        }

        validarCadastro(ativoFinanceiroRedeAtualizado);

        AtivoFinanceiro ativoFinanceiro = buscarAtivoObrigatorio(ativoFinanceiroRedeAtualizado.ativoFinanceiro);
        Rede rede = buscarRedeObrigatoria(ativoFinanceiroRedeAtualizado.rede);

        if (ativoFinanceiroRedeRepository.existsByAtivoAndRedeExcludingId(ativoFinanceiro.id, rede.id, id)) {
            throw new IllegalArgumentException("Ja existe uma configuracao para o ativo e rede informados");
        }

        ativoFinanceiroRede.ativoFinanceiro = ativoFinanceiro;
        ativoFinanceiroRede.rede = rede;
        ativoFinanceiroRede.identificador = ativoFinanceiroRedeAtualizado.identificador.trim();
        ativoFinanceiroRede.quantidadeCasasDecimais = normalizarQuantidadeCasasDecimais(ativoFinanceiroRedeAtualizado.quantidadeCasasDecimais);

        ativoFinanceiroRedeRepository.persist(ativoFinanceiroRede);
        return ativoFinanceiroRede;
    }

    @Transactional
    public void excluir(Long id) {
        ativoFinanceiroRedeRepository.deleteById(id);
    }

    private void validarCadastro(AtivoFinanceiroRede ativoFinanceiroRede) {
        if (ativoFinanceiroRede == null) {
            throw new IllegalArgumentException("Dados da configuracao ativo rede nao informados");
        }

        if (ativoFinanceiroRede.ativoFinanceiro == null || ativoFinanceiroRede.ativoFinanceiro.id == null) {
            throw new IllegalArgumentException("Ativo financeiro e obrigatorio");
        }

        if (ativoFinanceiroRede.rede == null || ativoFinanceiroRede.rede.id == null) {
            throw new IllegalArgumentException("Rede e obrigatoria");
        }

        if (ativoFinanceiroRede.identificador == null || ativoFinanceiroRede.identificador.trim().isEmpty()) {
            throw new IllegalArgumentException("Identificador e obrigatorio");
        }

        if (ativoFinanceiroRede.identificador.trim().length() > 150) {
            throw new IllegalArgumentException("Identificador deve ter no maximo 150 caracteres");
        }

        if (ativoFinanceiroRede.quantidadeCasasDecimais != null && ativoFinanceiroRede.quantidadeCasasDecimais < 0) {
            throw new IllegalArgumentException("Quantidade de casas decimais deve ser maior ou igual a zero");
        }
    }

    private AtivoFinanceiro buscarAtivoObrigatorio(AtivoFinanceiro ativoFinanceiro) {
        return ativoFinanceiroRepository.findByIdOptional(ativoFinanceiro.id)
                .orElseThrow(() -> new IllegalArgumentException("Ativo financeiro nao encontrado"));
    }

    private Rede buscarRedeObrigatoria(Rede rede) {
        return redeRepository.findByIdOptional(rede.id)
                .orElseThrow(() -> new IllegalArgumentException("Rede nao encontrada"));
    }

    private Integer normalizarQuantidadeCasasDecimais(Integer quantidadeCasasDecimais) {
        if (quantidadeCasasDecimais == null) {
            return 6;
        }
        return quantidadeCasasDecimais;
    }
}
