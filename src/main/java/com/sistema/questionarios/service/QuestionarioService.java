package com.sistema.questionarios.service;

import com.sistema.questionarios.dto.ResultadoDTO;
import com.sistema.questionarios.model.*;
import com.sistema.questionarios.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuestionarioService {

    private final TokenAcessoRepository tokenRepository;
    private final AlternativaRepository alternativaRepository;
    private final EmailService emailService; // Serviço que você criará para o RF09

    public QuestionarioService(TokenAcessoRepository tokenRepository,
                               AlternativaRepository alternativaRepository,
                               EmailService emailService) {
        this.tokenRepository = tokenRepository;
        this.alternativaRepository = alternativaRepository;
        this.emailService = emailService;
    }

    // RF07 e RF08: Associar Questionário ao Aluno e Gerar Token
    @Transactional
    public void enviarQuestionarioParaAluno(Questionario questionario, Aluno aluno) {
        TokenAcesso token = new TokenAcesso();
        token.setAluno(aluno);
        token.setQuestionario(questionario);

        // RN03: Expiração do token configurável
        token.setDataExpiracao(LocalDateTime.now().plusMinutes(questionario.getTempoValidadeTokenMinutos()));

        tokenRepository.save(token);

        // RF09: Enviar email (Método abstrato implementado pelo Spring Mail)
        emailService.enviarEmailComToken(aluno.getEmail(), token.getToken(), questionario.getTempoValidadeTokenMinutos());
    }

    // RF10, RF11 e RF12: Validação e Correção
    @Transactional
    public ResultadoDTO corrigirQuestionario(String tokenStr, List<Long> idsAlternativasSelecionadas) {
        TokenAcesso token = tokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new RuntimeException("Token inválido ou não encontrado."));

        // RN03 e RN04: Validações do Token
        if (token.getUtilizado()) {
            throw new RuntimeException("Questionário já foi respondido. (RN04)");
        }
        if (LocalDateTime.now().isAfter(token.getDataExpiracao())) {
            throw new RuntimeException("O token de acesso expirou. (RN03)");
        }

        double notaFinal = 0.0;
        int acertos = 0;
        int erros = 0;

        // RN06: Correção Automática
        for (Long idAlternativa : idsAlternativasSelecionadas) {
            Alternativa alternativa = alternativaRepository.findById(idAlternativa)
                    .orElseThrow(() -> new RuntimeException("Alternativa não encontrada."));

            if (alternativa.getCorreta()) {
                notaFinal += alternativa.getPergunta().getPontuacao();
                acertos++;
            } else {
                erros++;
            }
        }

        // Marca como utilizado
        token.setUtilizado(true);
        tokenRepository.save(token);

        // RF13: Retornando DTO com resultado
        double percentual = ((double) acertos / (acertos + erros)) * 100;
        return new ResultadoDTO(notaFinal, acertos, erros, percentual);
    }
}