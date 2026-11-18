package br.edu.aula.pedequintal.repository;

import br.edu.aula.pedequintal.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransacaoRepository extends JpaRepository<Transacao, Integer> {

    boolean existsByPropostaId(Integer propostaId);
}
