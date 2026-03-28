package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.Usuario;
import org.acme.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UsuarioService {

    @Inject
    UsuarioRepository usuarioRepository;

    public List<Usuario> listarTodos() {
        return usuarioRepository.listAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findByIdOptional(id);
    }

    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Transactional
    public Usuario criar(Usuario usuario) {
        if (usuarioRepository.existsByUsername(usuario.username)) {
            throw new IllegalArgumentException("Username já existe");
        }
        if (usuarioRepository.existsByEmail(usuario.email)) {
            throw new IllegalArgumentException("Email já existe");
        }
        usuario.password = Usuario.hashPassword(usuario.password);
        usuarioRepository.persist(usuario);
        return usuario;
    }

    @Transactional
    public Usuario atualizar(Long id, Usuario usuarioAtualizado) {
        Usuario usuario = usuarioRepository.findById(id);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        usuario.username = usuarioAtualizado.username;
        usuario.email = usuarioAtualizado.email;
        usuario.ativo = usuarioAtualizado.ativo;
        if (usuarioAtualizado.password != null && !usuarioAtualizado.password.isEmpty()) {
            usuario.password = Usuario.hashPassword(usuarioAtualizado.password);
        }
        usuarioRepository.persist(usuario);
        return usuario;
    }

    @Transactional
    public void excluir(Long id) {
        usuarioRepository.deleteById(id);
    }

    public boolean validarCredenciais(String username, String password) {
        Usuario usuario = usuarioRepository.findByUsername(username);
        return usuario != null && usuario.checkPassword(password);
    }
}
