package br.edu.aula.pedequintal.repository;

import br.edu.aula.pedequintal.model.Proposta;
import br.edu.aula.pedequintal.model.StatusProposta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PropostaRepository extends JpaRepository<Proposta, Integer> {

    /** Recebidas: propostas feitas nos anúncios do usuário. */
    List<Proposta> findByAnuncioUsuarioIdOrderByDataCriacaoDesc(Integer usuarioId);

    /** Enviadas: propostas que o usuário fez. */
    List<Proposta> findByProponenteIdOrderByDataCriacaoDesc(Integer usuarioId);

    /** Demais propostas de um anúncio em determinado status (usado no aceite). */
    List<Proposta> findByAnuncioIdAndStatus(Integer anuncioId, StatusProposta status);

    /** Proposta aceita de um anúncio (no máximo uma). */
    Optional<Proposta> findFirstByAnuncioIdAndStatus(Integer anuncioId, StatusProposta status);

    /** Impede o mesmo usuário de mandar duas propostas pendentes para o mesmo item. */
    boolean existsByAnuncioIdAndProponenteIdAndStatus(Integer anuncioId, Integer proponenteId, StatusProposta status);

    long countByAnuncioUsuarioIdAndStatus(Integer usuarioId, StatusProposta status);
}
