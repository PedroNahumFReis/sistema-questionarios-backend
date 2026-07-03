package com.sistema.questionarios.service;

import com.sistema.questionarios.dto.EstatisticaDTO;
import com.sistema.questionarios.dto.QuestionarioResponderDTO;
import com.sistema.questionarios.dto.RespostaViewDTO;
import com.sistema.questionarios.dto.ResultadoDTO;
import com.sistema.questionarios.model.*;
import com.sistema.questionarios.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serviço com as regras de negócio dos questionários.
 *
 * A camada Service concentra a lógica (criação, envio, correção, estatísticas), deixando os
 * Controllers só com o trabalho de receber a requisição e devolver a resposta.
 *
 * @Transactional (nos métodos): tudo dentro do método roda em uma única transação de banco.
 * Se der erro no meio, o Spring desfaz (rollback) o que foi salvo, mantendo o banco consistente.
 */
@Service
public class QuestionarioService {

    // Repositórios injetados pelo Spring (injeção via construtor), para acessar cada tabela.
    private final TokenAcessoRepository tokenRepository;
    private final AlternativaRepository alternativaRepository;
    private final RespostaAlunoRepository respostaRepository;
    private final QuestionarioRepository questionarioRepository;
    private final PerguntaRepository perguntaRepository;
    private final EmailService emailService;

    public QuestionarioService(TokenAcessoRepository tokenRepository,
                               AlternativaRepository alternativaRepository,
                               RespostaAlunoRepository respostaRepository,
                               QuestionarioRepository questionarioRepository,
                               PerguntaRepository perguntaRepository,
                               EmailService emailService) {
        this.tokenRepository = tokenRepository;
        this.alternativaRepository = alternativaRepository;
        this.respostaRepository = respostaRepository;
        this.questionarioRepository = questionarioRepository;
        this.perguntaRepository = perguntaRepository;
        this.emailService = emailService;
    }

    /**
     * RF04: Cria o questionário. Antes de salvar, valida cada pergunta (RN05) e liga as relações
     * entre questionário -> perguntas -> alternativas.
     */
    @Transactional
    public Questionario criarQuestionario(Questionario questionario) {
        if (questionario.getPerguntas() != null) {
            for (Pergunta pergunta : questionario.getPerguntas()) {
                pergunta.setQuestionario(questionario); // liga a pergunta ao questionário pai
                validarERelacionarPergunta(pergunta);
            }
        }
        // save() com cascade = ALL salva o questionário e, junto, suas perguntas e alternativas.
        return questionarioRepository.save(questionario);
    }

    /**
     * RF05: Adiciona (e persiste) uma pergunta a um questionário já existente.
     */
    @Transactional
    public Pergunta adicionarPergunta(Long questionarioId, Pergunta pergunta) {
        // Busca o questionário; se não existir, lança erro (vira 404/500 dependendo do tratamento).
        Questionario questionario = questionarioRepository.findById(questionarioId)
                .orElseThrow(() -> new RuntimeException("Questionário não encontrado."));
        pergunta.setQuestionario(questionario);
        validarERelacionarPergunta(pergunta);
        return perguntaRepository.save(pergunta);
    }

    /**
     * RN05: garante que cada pergunta tenha ao menos uma alternativa correta.
     * Também liga cada alternativa à sua pergunta — sem isso, a chave estrangeira (pergunta_id)
     * ficaria nula ao salvar em cascata.
     * Lança IllegalArgumentException, que o GlobalExceptionHandler converte em HTTP 400.
     */
    private void validarERelacionarPergunta(Pergunta pergunta) {
        List<Alternativa> alternativas = pergunta.getAlternativas();
        if (alternativas == null || alternativas.isEmpty()) {
            throw new IllegalArgumentException("RN05: cada pergunta deve possuir ao menos uma alternativa.");
        }
        // anyMatch: retorna true se PELO MENOS UMA alternativa tiver correta == true.
        boolean temCorreta = alternativas.stream()
                .anyMatch(a -> Boolean.TRUE.equals(a.getCorreta()));
        if (!temCorreta) {
            throw new IllegalArgumentException("RN05: cada pergunta deve possuir ao menos uma alternativa correta.");
        }
        for (Alternativa alternativa : alternativas) {
            alternativa.setPergunta(pergunta); // liga a alternativa à pergunta pai
        }
    }

    /**
     * RF07 e RF08: associa o questionário a um aluno e gera o token de acesso; dispara o e-mail (RF09).
     * Retorna a string do token (útil para testes e para o professor, já que o SMTP é fictício).
     */
    @Transactional
    public String enviarQuestionarioParaAluno(Questionario questionario, Aluno aluno) {
        TokenAcesso token = new TokenAcesso();
        token.setAluno(aluno);
        token.setQuestionario(questionario);

        // RNF05/RN03: a expiração é "agora + tempo configurado no questionário" (em minutos).
        token.setDataExpiracao(LocalDateTime.now().plusMinutes(questionario.getTempoValidadeTokenMinutos()));

        tokenRepository.save(token);

        // RF09: envia o e-mail. Uma falha de SMTP é tratada dentro do EmailService e não derruba o fluxo.
        emailService.enviarEmailComToken(aluno.getEmail(), token.getToken(), questionario.getTempoValidadeTokenMinutos());

        return token.getToken();
    }

    /**
     * RF10, RF11, RF12: valida o token, corrige as respostas automaticamente e calcula a nota.
     */
    @Transactional
    public ResultadoDTO corrigirQuestionario(String tokenStr, List<Long> idsAlternativasSelecionadas) {
        // RF10: o token precisa existir. IllegalArgumentException vira HTTP 400 com mensagem clara.
        TokenAcesso token = tokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou não encontrado."));

        // RN04: não pode responder duas vezes.
        if (token.getUtilizado()) {
            throw new IllegalArgumentException("Questionário já foi respondido. (RN04)");
        }
        // RN03: não pode responder após a expiração.
        if (LocalDateTime.now().isAfter(token.getDataExpiracao())) {
            throw new IllegalArgumentException("O token de acesso expirou. (RN03)");
        }

        double notaFinal = 0.0;
        int acertos = 0;
        int erros = 0;

        // RF12/RN06: percorre cada alternativa marcada, verifica se está correta e vai somando.
        for (Long idAlternativa : idsAlternativasSelecionadas) {
            Alternativa alternativa = alternativaRepository.findById(idAlternativa)
                    .orElseThrow(() -> new RuntimeException("Alternativa não encontrada."));

            boolean acertou = Boolean.TRUE.equals(alternativa.getCorreta());
            if (acertou) {
                notaFinal += alternativa.getPergunta().getPontuacao(); // acertou: soma os pontos da pergunta
                acertos++;
            } else {
                erros++;
            }

            // RF14: guarda a resposta do aluno no banco, para depois consultar e gerar estatísticas.
            RespostaAluno resposta = new RespostaAluno();
            resposta.setToken(token);
            resposta.setPergunta(alternativa.getPergunta());
            resposta.setAlternativaSelecionada(alternativa);
            resposta.setCorreta(acertou);
            respostaRepository.save(resposta);
        }

        // Marca o token como usado, cumprindo a RN04 nas próximas tentativas.
        token.setUtilizado(true);
        tokenRepository.save(token);

        // RF13: monta o resultado. Protege contra divisão por zero (se a lista vier vazia).
        int totalRespondidas = acertos + erros;
        double percentual = totalRespondidas == 0 ? 0.0 : ((double) acertos / totalRespondidas) * 100;
        return new ResultadoDTO(notaFinal, acertos, erros, percentual, token.getQuestionario().getId());
    }

    /**
     * Entrega o questionário ao aluno para ser respondido (usado pelo frontend do aluno).
     * Valida o token (existe, não usado, não expirado) e devolve as perguntas SEM a resposta correta.
     */
    @Transactional(readOnly = true)
    public QuestionarioResponderDTO buscarParaResponder(String tokenStr) {
        TokenAcesso token = tokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou não encontrado."));
        if (token.getUtilizado()) {
            throw new IllegalArgumentException("Este questionário já foi respondido. (RN04)");
        }
        if (LocalDateTime.now().isAfter(token.getDataExpiracao())) {
            throw new IllegalArgumentException("O link de acesso expirou. (RN03)");
        }

        Questionario q = token.getQuestionario();

        // Converte as entidades para o DTO, omitindo o campo 'correta' de cada alternativa.
        List<QuestionarioResponderDTO.PerguntaResponder> perguntas = q.getPerguntas().stream()
                .map(p -> new QuestionarioResponderDTO.PerguntaResponder(
                        p.getId(),
                        p.getEnunciado(),
                        p.getTipo() == null ? null : p.getTipo().name(),
                        p.getAlternativas().stream()
                                .map(a -> new QuestionarioResponderDTO.AlternativaResponder(a.getId(), a.getTexto()))
                                .toList()))
                .toList();

        return new QuestionarioResponderDTO(q.getId(), q.getTitulo(), q.getDescricao(), perguntas);
    }

    /**
     * RF14: lista as respostas que um aluno enviou, identificadas pelo token usado.
     * readOnly = true: transação apenas de leitura (uma pequena otimização, não altera nada).
     */
    @Transactional(readOnly = true)
    public List<RespostaViewDTO> listarRespostas(String tokenStr) {
        TokenAcesso token = tokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou não encontrado."));

        // Converte cada RespostaAluno (entidade) em um RespostaViewDTO (só os campos de exibição).
        return respostaRepository.findByToken(token).stream()
                .map(r -> new RespostaViewDTO(
                        r.getPergunta().getId(),
                        r.getPergunta().getEnunciado(),
                        r.getAlternativaSelecionada().getId(),
                        r.getAlternativaSelecionada().getTexto(),
                        r.getCorreta()))
                .toList();
    }

    /**
     * RF15: calcula as estatísticas do questionário — números gerais e um detalhamento por pergunta.
     */
    @Transactional(readOnly = true)
    public EstatisticaDTO calcularEstatisticas(Long questionarioId) {
        // Todas as respostas já dadas neste questionário (de todos os alunos).
        List<RespostaAluno> respostas = respostaRepository.findByToken_Questionario_Id(questionarioId);

        // Totais gerais.
        long total = respostas.size();
        long corretas = respostas.stream().filter(r -> Boolean.TRUE.equals(r.getCorreta())).count();
        long incorretas = total - corretas;
        double percAcertos = total == 0 ? 0.0 : (corretas * 100.0) / total;
        double percErros = total == 0 ? 0.0 : (incorretas * 100.0) / total;

        // Agrupa as respostas por pergunta: Map<Pergunta, lista de respostas daquela pergunta>.
        Map<Pergunta, List<RespostaAluno>> porPergunta = respostas.stream()
                .collect(Collectors.groupingBy(RespostaAluno::getPergunta));

        // Para cada pergunta, calcula seus próprios acertos/erros e percentuais.
        List<EstatisticaDTO.EstatisticaPergunta> detalhes = porPergunta.entrySet().stream()
                .map(entry -> {
                    Pergunta p = entry.getKey();
                    List<RespostaAluno> lista = entry.getValue();
                    long tot = lista.size();
                    long ok = lista.stream().filter(r -> Boolean.TRUE.equals(r.getCorreta())).count();
                    long nok = tot - ok;
                    double pOk = tot == 0 ? 0.0 : (ok * 100.0) / tot;
                    double pErr = tot == 0 ? 0.0 : (nok * 100.0) / tot;
                    return new EstatisticaDTO.EstatisticaPergunta(p.getId(), p.getEnunciado(), ok, nok, pOk, pErr);
                })
                // Ordena por id da pergunta só para a saída ficar previsível/organizada.
                .sorted(Comparator.comparing(EstatisticaDTO.EstatisticaPergunta::getPerguntaId))
                .toList();

        return new EstatisticaDTO(questionarioId, total, corretas, incorretas, percAcertos, percErros, detalhes);
    }
}
