package com.helpdesk.repository;

import com.helpdesk.model.Perfil;
import com.helpdesk.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    long countByPerfilAndAtivoTrue(Perfil perfil);

    List<Usuario> findAllByOrderByNomeAsc();

    List<Usuario> findByPerfilInAndAtivoTrueOrderByNomeAsc(Collection<Perfil> perfis);
}
