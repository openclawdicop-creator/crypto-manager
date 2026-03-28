package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.AtivoFinanceiro;
import org.acme.repository.AtivoFinanceiroRepository;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AtivoFinanceiroService {

    @Inject
    AtivoFinanceiroRepository ativoFinanceiroRepository;

    public List<AtivoFinanceiro> listarTodos() {
        return ativoFinanceiroRepository.listAll();
    }

    public Optional<AtivoFinanceiro> buscarPorId(Long id) {
        return ativoFinanceiroRepository.findByIdOptional(id);
    }

    public AtivoFinanceiro buscarPorSimbolo(String simbolo) {
        return ativoFinanceiroRepository.findBySimbolo(simbolo);
    }

    @Transactional
    public AtivoFinanceiro criar(AtivoFinanceiro ativoFinanceiro) {
        if (ativoFinanceiroRepository.existsBySimbolo(ativoFinanceiro.simbolo)) {
            throw new IllegalArgumentException("Símbolo já existe");
        }
        ativoFinanceiroRepository.persist(ativoFinanceiro);
        return ativoFinanceiro;
    }

    @Transactional
    public AtivoFinanceiro atualizar(Long id, AtivoFinanceiro ativoFinanceiroAtualizado) {
        AtivoFinanceiro ativoFinanceiro = ativoFinanceiroRepository.findById(id);
        if (ativoFinanceiro == null) {
            throw new IllegalArgumentException("Ativo financeiro não encontrado");
        }
        ativoFinanceiro.nome = ativoFinanceiroAtualizado.nome;
        ativoFinanceiro.simbolo = ativoFinanceiroAtualizado.simbolo;
        ativoFinanceiroRepository.persist(ativoFinanceiro);
        return ativoFinanceiro;
    }

    @Transactional
    public void excluir(Long id) {
        ativoFinanceiroRepository.deleteById(id);
    }
}
