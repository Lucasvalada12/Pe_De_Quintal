package br.edu.aula.pedequintal.service;

/**
 * Erro "esperado" de regra de negócio (ex.: aceitar proposta de outro usuário).
 * O controller captura e mostra a mensagem como flash message.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
