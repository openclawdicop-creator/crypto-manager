package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.Proxy;
import org.acme.repository.ProxyRepository;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProxyService {

    @Inject
    ProxyRepository proxyRepository;

    public List<Proxy> listarTodos() {
        return proxyRepository.listAll();
    }

    public Optional<Proxy> buscarPorId(Long id) {
        return proxyRepository.findByIdOptional(id);
    }

    @Transactional
    public Proxy criar(Proxy proxy) {
        proxyRepository.persist(proxy);
        return proxy;
    }

    @Transactional
    public Proxy atualizar(Long id, Proxy proxyAtualizado) {
        Proxy proxy = proxyRepository.findById(id);
        if (proxy == null) {
            throw new IllegalArgumentException("Proxy não encontrado");
        }
        proxy.nome = proxyAtualizado.nome;
        proxy.url = proxyAtualizado.url;
        proxy.usuario = proxyAtualizado.usuario;
        proxy.senha = proxyAtualizado.senha;
        proxy.token = proxyAtualizado.token;
        proxyRepository.persist(proxy);
        return proxy;
    }

    @Transactional
    public void excluir(Long id) {
        proxyRepository.deleteById(id);
    }
}
