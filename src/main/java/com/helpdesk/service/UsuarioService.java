package com.helpdesk.service;

import com.helpdesk.dto.UsuarioForm;
import com.helpdesk.model.Perfil;
import com.helpdesk.model.Usuario;
import com.helpdesk.repository.UsuarioRepository;
import com.helpdesk.util.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    @Transactional(readOnly = true)
    public Usuario buscar(Long id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    @Transactional
    public Usuario criar(UsuarioForm f) {
        Usuario u = new Usuario();
        u.setNome(f.getNome().trim());
        u.setEmail(f.getEmail().trim().toLowerCase());
        u.setPerfil(f.getPerfil());
        u.setAtivo(f.isAtivo());
        u.setSenha(encoder.encode(f.getSenha()));
        return usuarios.save(u);
    }

    @Transactional
    public Usuario atualizar(Long id, UsuarioForm f, String emailLogado) {
        Usuario u = buscar(id);
        validarPerdaDeAdmin(u, f.getPerfil(), f.isAtivo(), emailLogado);
        u.setNome(f.getNome().trim());
        u.setEmail(f.getEmail().trim().toLowerCase());
        u.setPerfil(f.getPerfil());
        u.setAtivo(f.isAtivo());
        if (f.getSenha() != null && !f.getSenha().isBlank()) {
            u.setSenha(encoder.encode(f.getSenha()));
        }
        return usuarios.save(u);
    }

    @Transactional
    public Usuario alternarAtivo(Long id, String emailLogado) {
        Usuario u = buscar(id);
        boolean novoValor = !Boolean.TRUE.equals(u.getAtivo());
        validarPerdaDeAdmin(u, u.getPerfil(), novoValor, emailLogado);
        u.setAtivo(novoValor);
        return usuarios.save(u);
    }

    /** Impede que o admin se bloqueie ou que o sistema fique sem administrador ativo. */
    private void validarPerdaDeAdmin(Usuario atual, Perfil novoPerfil, boolean novoAtivo, String emailLogado) {
        boolean eraAdminAtivo = atual.getPerfil() == Perfil.ADMIN && Boolean.TRUE.equals(atual.getAtivo());
        boolean continuaAdminAtivo = novoPerfil == Perfil.ADMIN && novoAtivo;
        if (!eraAdminAtivo || continuaAdminAtivo) return;

        if (atual.getEmail().equalsIgnoreCase(emailLogado)) {
            throw new RegraNegocioException("Você não pode remover o seu próprio acesso de administrador.");
        }
        if (usuarios.countByPerfilAndAtivoTrue(Perfil.ADMIN) <= 1) {
            throw new RegraNegocioException("O sistema precisa de pelo menos um administrador ativo.");
        }
    }
}
