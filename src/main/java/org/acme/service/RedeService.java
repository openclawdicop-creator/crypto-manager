package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.Rede;
import org.acme.repository.RedeRepository;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RedeService {

    @Inject
    RedeRepository redeRepository;

    public List<Rede> listarTodos() {
        return redeRepository.listAll();
    }

    public Optional<Rede> buscarPorId(Long id) {
        return redeRepository.findByIdOptional(id);
    }

    @Transactional
    public Rede criar(Rede rede) {
        redeRepository.persist(rede);
        return rede;
    }

    @Transactional
    public Rede atualizar(Long id, Rede redeAtualizada) {
        Rede rede = redeRepository.findById(id);
        if (rede == null) {
            throw new IllegalArgumentException("Rede não encontrada");
        }
        rede.nome = redeAtualizada.nome;
        rede.urlExplorer = redeAtualizada.urlExplorer;
        redeRepository.persist(rede);
        return rede;
    }

    @Transactional
    public void excluir(Long id) {
        redeRepository.deleteById(id);
    }
}
