package br.edu.aula.pedequintal.service;

import br.edu.aula.pedequintal.model.Anuncio;
import br.edu.aula.pedequintal.model.StatusAnuncio;
import br.edu.aula.pedequintal.model.Usuario;
import br.edu.aula.pedequintal.repository.AnuncioRepository;
import br.edu.aula.pedequintal.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnuncioServiceTest {

    @Mock AnuncioRepository anuncios;
    @Mock CategoriaRepository categorias;
    @Mock FotoService fotoService;

    @InjectMocks AnuncioService service;

    Usuario dono;
    Usuario outro;
    Anuncio anuncio;

    @BeforeEach
    void setUp() {
        dono = new Usuario();
        dono.setId(1);
        outro = new Usuario();
        outro.setId(2);

        anuncio = new Anuncio();
        anuncio.setId(10);
        anuncio.setUsuario(dono);
        anuncio.setStatus(StatusAnuncio.ATIVO);
    }

    @Test
    @DisplayName("Excluir: dono exclui anúncio ATIVO")
    void donoExcluiAnuncioAtivo() {
        when(anuncios.findById(10)).thenReturn(Optional.of(anuncio));

        service.excluir(10, dono);

        verify(anuncios).delete(anuncio);
    }

    @Test
    @DisplayName("Excluir: ninguém exclui anúncio de outra pessoa")
    void outroUsuarioNaoExclui() {
        when(anuncios.findById(10)).thenReturn(Optional.of(anuncio));

        assertThatThrownBy(() -> service.excluir(10, outro)).isInstanceOf(RegraNegocioException.class);
        verify(anuncios, never()).delete(any());
    }

    @Test
    @DisplayName("Excluir: anúncio RESERVADO não pode ser excluído")
    void reservadoNaoPodeSerExcluido() {
        anuncio.setStatus(StatusAnuncio.RESERVADO);
        when(anuncios.findById(10)).thenReturn(Optional.of(anuncio));

        assertThatThrownBy(() -> service.excluir(10, dono)).isInstanceOf(RegraNegocioException.class);
        verify(anuncios, never()).delete(any());
    }
}
