package br.edu.aula.pedequintal.config;

import br.edu.aula.pedequintal.model.Anuncio;
import br.edu.aula.pedequintal.model.Categoria;
import br.edu.aula.pedequintal.model.Modalidade;
import br.edu.aula.pedequintal.model.StatusAnuncio;
import br.edu.aula.pedequintal.model.Usuario;
import br.edu.aula.pedequintal.repository.AnuncioRepository;
import br.edu.aula.pedequintal.repository.CategoriaRepository;
import br.edu.aula.pedequintal.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Cria usuários e anúncios de exemplo na primeira execução (banco sem usuários).
 * Ligado por app.dados-exemplo=true (variável APP_DADOS_EXEMPLO no .env).
 * Senha de todos: 123456
 */
@Component
@ConditionalOnProperty(name = "app.dados-exemplo", havingValue = "true")
public class DadosExemploLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosExemploLoader.class);

    private final UsuarioRepository usuarios;
    private final CategoriaRepository categorias;
    private final AnuncioRepository anuncios;
    private final PasswordEncoder passwordEncoder;

    public DadosExemploLoader(UsuarioRepository usuarios, CategoriaRepository categorias,
                              AnuncioRepository anuncios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.categorias = categorias;
        this.anuncios = anuncios;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarios.count() > 0) {
            return;   // já tem dados: não mexe em nada
        }
        String senha = passwordEncoder.encode("123456");

        Usuario maria = usuarios.save(new Usuario("Maria Souza", "maria@pedequintal.com", senha, "(31) 99999-1001", "Sete Lagoas", "MG"));
        Usuario joao  = usuarios.save(new Usuario("João Pereira", "joao@pedequintal.com", senha, "(31) 99999-1002", "Sete Lagoas", "MG"));
        Usuario ana   = usuarios.save(new Usuario("Ana Lima", "ana@pedequintal.com", senha, "(31) 99999-1003", "Belo Horizonte", "MG"));

        Categoria hortalica = categorias.findByNome("Hortaliça").orElseThrow();
        Categoria fruta     = categorias.findByNome("Fruta").orElseThrow();
        Categoria tempero   = categorias.findByNome("Tempero").orElseThrow();
        Categoria muda      = categorias.findByNome("Muda").orElseThrow();

        criar(maria, hortalica, "Alface crespa", "Colhida hoje, sem agrotóxico.", "6", "pé",
                Modalidade.DOACAO, null, "Centro — Sete Lagoas/MG");
        criar(joao, fruta, "Limão taiti", "Colhidos do pé do fundo do quintal. Prefiro trocar por cheiro-verde ou pimentão.",
                "3", "kg", Modalidade.TROCA, "Cheiro-verde, pimentão", "Centro — Sete Lagoas/MG");
        criar(ana, hortalica, "Tomate cereja", "Bem docinho, colhido ontem.", "800", "g",
                Modalidade.TROCA, "Mudas de temperos", "Savassi — Belo Horizonte/MG");
        criar(maria, tempero, "Manjericão", "Maço grande e cheiroso.", "2", "maço",
                Modalidade.DOACAO, null, "Centro — Sete Lagoas/MG");
        criar(joao, muda, "Muda de pitanga", "Muda com 40 cm, pronta para plantar.", "1", "muda",
                Modalidade.TROCA, "Mudas de frutíferas", "Jardim Arizona — Sete Lagoas/MG");

        log.info("Dados de exemplo criados: maria@ / joao@ / ana@pedequintal.com (senha 123456)");
    }

    private void criar(Usuario dono, Categoria categoria, String titulo, String descricao, String quantidade,
                       String unidade, Modalidade modalidade, String desejaReceber, String local) {
        Anuncio a = new Anuncio();
        a.setUsuario(dono);
        a.setCategoria(categoria);
        a.setTitulo(titulo);
        a.setDescricao(descricao);
        a.setQuantidade(new BigDecimal(quantidade));
        a.setUnidade(unidade);
        a.setModalidade(modalidade);
        a.setDesejaReceber(desejaReceber);
        a.setLocalRetirada(local);
        a.setDisponivelAte(LocalDate.now().plusDays(15));
        a.setStatus(StatusAnuncio.ATIVO);
        a.setDataCriacao(LocalDateTime.now());
        anuncios.save(a);
    }
}
