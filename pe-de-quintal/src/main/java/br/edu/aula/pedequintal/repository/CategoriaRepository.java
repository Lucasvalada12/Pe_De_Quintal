package br.edu.aula.pedequintal.repository;

import br.edu.aula.pedequintal.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    List<Categoria> findAllByOrderByNome();

    Optional<Categoria> findByNome(String nome);
}
