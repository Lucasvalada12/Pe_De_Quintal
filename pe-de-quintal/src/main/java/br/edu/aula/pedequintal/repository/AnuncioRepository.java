package br.edu.aula.pedequintal.repository;

import br.edu.aula.pedequintal.model.Anuncio;
import br.edu.aula.pedequintal.model.StatusAnuncio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AnuncioRepository extends JpaRepository<Anuncio, Integer> {

    /*
     * Lista de itens (W2) com busca e filtros opcionais.
     * - busca vazia ("")     -> não filtra por texto
     * - categoriaId = 0      -> todas as categorias
     * - cidade vazia ("")    -> todas as cidades
     * Usamos "" e 0 em vez de null porque o PostgreSQL não consegue
     * descobrir o tipo de um parâmetro nulo em "(:x IS NULL OR ...)".
     * JOIN FETCH traz anunciante e categoria no mesmo SELECT (menos idas ao Supabase).
     */
    @Query("""
            SELECT a FROM Anuncio a
              JOIN FETCH a.usuario u
              JOIN FETCH a.categoria c
             WHERE a.status = :status
               AND a.disponivelAte >= :hoje
               AND (:categoriaId = 0 OR c.id = :categoriaId)
               AND (:cidade = '' OR LOWER(u.cidade) = LOWER(:cidade))
               AND (:busca = ''
                    OR LOWER(a.titulo) LIKE LOWER(CONCAT('%', :busca, '%'))
                    OR LOWER(a.descricao) LIKE LOWER(CONCAT('%', :busca, '%')))
             ORDER BY a.dataCriacao DESC
            """)
    List<Anuncio> buscarDisponiveis(@Param("status") StatusAnuncio status,
                                    @Param("hoje") LocalDate hoje,
                                    @Param("busca") String busca,
                                    @Param("categoriaId") Integer categoriaId,
                                    @Param("cidade") String cidade);

    /** Cidades que possuem pelo menos um anúncio ativo (alimenta o filtro "Cidade"). */
    @Query("""
            SELECT DISTINCT a.usuario.cidade FROM Anuncio a
             WHERE a.status = :status AND a.disponivelAte >= :hoje
             ORDER BY a.usuario.cidade
            """)
    List<String> listarCidadesComAnuncios(@Param("status") StatusAnuncio status,
                                          @Param("hoje") LocalDate hoje);

    /** "Meus anúncios" (tabela da W4). */
    List<Anuncio> findByUsuarioIdOrderByDataCriacaoDesc(Integer usuarioId);
}
