package com.helpdesk.repository;

import com.helpdesk.model.MotivoChamado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MotivoChamadoRepository extends JpaRepository<MotivoChamado, Long> {

    List<MotivoChamado> findAllByOrderByNomeAsc();

    List<MotivoChamado> findByAtivoTrueOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
