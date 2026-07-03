package com.sistema.questionarios.repository;

import com.sistema.questionarios.model.RespostaAluno;
import com.sistema.questionarios.model.TokenAcesso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositório de RespostaAluno. Fornece as consultas usadas pelo RF14 e RF15.
 */
public interface RespostaAlunoRepository extends JpaRepository<RespostaAluno, Long> {

    // RF14: todas as respostas ligadas a um token (ou seja, de um aluno naquele questionário).
    List<RespostaAluno> findByToken(TokenAcesso token);

    // RF15: todas as respostas de um questionário, navegando pelas relações
    // RespostaAluno -> token -> questionario -> id. O underline separa cada "salto" de propriedade,
    // e o Spring Data monta o JOIN automaticamente a partir do nome do método.
    List<RespostaAluno> findByToken_Questionario_Id(Long questionarioId);
}
