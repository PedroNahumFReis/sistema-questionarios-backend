package com.sistema.questionarios.controller;

import com.sistema.questionarios.dto.RespostaAlunoDTO;
import com.sistema.questionarios.dto.ResultadoDTO;
import com.sistema.questionarios.model.Aluno;
import com.sistema.questionarios.model.Questionario;
import com.sistema.questionarios.repository.AlunoRepository;
import com.sistema.questionarios.repository.QuestionarioRepository;
import com.sistema.questionarios.service.QuestionarioService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Imports estáticos que permitem escrever linkTo(...) e methodOn(...) para montar os links HATEOAS.
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Controller central do sistema: cuida do questionário, perguntas, envio ao aluno,
 * resposta/correção, visualização de respostas e estatísticas.
 *
 * Note o padrão: o controller recebe a requisição, delega a regra de negócio ao
 * QuestionarioService e devolve a resposta (muitas vezes com links HATEOAS - RF17).
 */
@RestController
@RequestMapping("/api")
public class QuestionarioController {

    private final QuestionarioRepository questionarioRepository;
    private final AlunoRepository alunoRepository;
    private final QuestionarioService questionarioService;

    public QuestionarioController(QuestionarioRepository questionarioRepository,
                                  AlunoRepository alunoRepository,
                                  QuestionarioService questionarioService) {
        this.questionarioRepository = questionarioRepository;
        this.alunoRepository = alunoRepository;
        this.questionarioService = questionarioService;
    }

    // @Operation define o texto que aparece na documentação Swagger (RF16) para este endpoint.
    @Operation(summary = "Criar um novo questionário (Apenas Professores)")
    @PostMapping("/questionarios")
    public ResponseEntity<EntityModel<Questionario>> criarQuestionario(@RequestBody Questionario questionario) {
        // RF04 + RN05: o service valida perguntas/alternativas e liga as relações antes de salvar.
        Questionario salvo = questionarioService.criarQuestionario(questionario);
        return ResponseEntity.ok(toResource(salvo));
    }

    @Operation(summary = "Listar todos os questionários (para o painel do professor)")
    @GetMapping("/questionarios")
    public ResponseEntity<java.util.List<Questionario>> listarQuestionarios() {
        return ResponseEntity.ok(questionarioRepository.findAll());
    }

    @Operation(summary = "Buscar questionário com suporte HATEOAS")
    @GetMapping("/questionarios/{id}")
    public ResponseEntity<EntityModel<Questionario>> buscarQuestionario(@PathVariable Long id) {
        // Optional.map: se achar, monta o recurso com links; se não, devolve 404.
        return questionarioRepository.findById(id)
                .map(questionario -> ResponseEntity.ok(toResource(questionario)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar as perguntas de um questionário")
    @GetMapping("/questionarios/{id}/perguntas")
    public ResponseEntity<java.util.List<com.sistema.questionarios.model.Pergunta>> listarPerguntas(@PathVariable Long id) {
        return questionarioRepository.findById(id)
                .map(q -> ResponseEntity.ok(q.getPerguntas()))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * RF17: método auxiliar que envolve um Questionario em um EntityModel e adiciona os links
     * de navegação (self, perguntas, estatisticas). linkTo(methodOn(...)) gera a URL a partir do
     * próprio método do controller — se a rota mudar, o link acompanha automaticamente.
     */
    private EntityModel<Questionario> toResource(Questionario questionario) {
        Long id = questionario.getId();
        EntityModel<Questionario> resource = EntityModel.of(questionario);
        resource.add(linkTo(methodOn(QuestionarioController.class).buscarQuestionario(id)).withSelfRel());
        resource.add(linkTo(methodOn(QuestionarioController.class).listarPerguntas(id)).withRel("perguntas"));
        resource.add(linkTo(methodOn(QuestionarioController.class).buscarEstatisticas(id)).withRel("estatisticas"));
        return resource;
    }

    @Operation(summary = "Associar questionário a um aluno e enviar e-mail com token")
    @PostMapping("/questionarios/{idQuestionario}/enviar/{idAluno}")
    public ResponseEntity<java.util.Map<String, String>> enviarParaAluno(@PathVariable Long idQuestionario, @PathVariable Long idAluno) {
        // RN02: só é possível enviar para um aluno já cadastrado (orElseThrow falha se não existir).
        Questionario questionario = questionarioRepository.findById(idQuestionario).orElseThrow();
        Aluno aluno = alunoRepository.findById(idAluno).orElseThrow();

        // RF07/RF08/RF09: gera o token e dispara o e-mail. Retornamos o token no corpo porque o
        // SMTP é fictício — assim dá para testar a resposta sem depender do e-mail chegar.
        String tokenGerado = questionarioService.enviarQuestionarioParaAluno(questionario, aluno);
        return ResponseEntity.ok(java.util.Map.of(
                "mensagem", "Questionário associado ao aluno e e-mail disparado.",
                "token", tokenGerado,
                "linkResposta", "/api/responder/" + tokenGerado));
    }

    @Operation(summary = "Aluno carrega o questionário para responder (perguntas sem a resposta correta)")
    @GetMapping("/responder/{token}")
    public ResponseEntity<com.sistema.questionarios.dto.QuestionarioResponderDTO> carregarParaResponder(
            @PathVariable String token) {
        return ResponseEntity.ok(questionarioService.buscarParaResponder(token));
    }

    @Operation(summary = "Aluno responde o questionário usando o token enviado por e-mail")
    @PostMapping("/responder/{token}")
    public ResponseEntity<EntityModel<ResultadoDTO>> responderQuestionario(
            @PathVariable String token,
            @RequestBody RespostaAlunoDTO respostas) {

        // O service valida o token (RN03, RN04) e faz a correção automática (RN06/RF12).
        ResultadoDTO resultado = questionarioService.corrigirQuestionario(token, respostas.getIdsAlternativasSelecionadas());

        // RF17: junto com a nota, o aluno recebe links para ver as respostas e as estatísticas
        // (conforme a Seção 2 do documento de requisitos).
        EntityModel<ResultadoDTO> resource = EntityModel.of(resultado);
        resource.add(linkTo(methodOn(QuestionarioController.class).visualizarRespostas(token)).withRel("respostas"));
        resource.add(linkTo(methodOn(QuestionarioController.class)
                .buscarEstatisticas(resultado.getQuestionarioId())).withRel("estatisticas"));
        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Adicionar pergunta a um questionário (RF05, RF06)")
    @PostMapping("/questionarios/{id}/perguntas")
    public ResponseEntity<EntityModel<com.sistema.questionarios.model.Pergunta>> adicionarPergunta(
            @PathVariable Long id,
            @RequestBody com.sistema.questionarios.model.Pergunta pergunta) {

        // RF05 + RN05: o service persiste a pergunta (com alternativas) validando alternativa correta.
        com.sistema.questionarios.model.Pergunta salva = questionarioService.adicionarPergunta(id, pergunta);

        EntityModel<com.sistema.questionarios.model.Pergunta> resource = EntityModel.of(salva);
        resource.add(linkTo(methodOn(QuestionarioController.class).buscarQuestionario(id)).withRel("questionario"));
        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Aluno visualiza as respostas que enviou (RF14)")
    @GetMapping("/respostas/{token}")
    public ResponseEntity<java.util.List<com.sistema.questionarios.dto.RespostaViewDTO>> visualizarRespostas(
            @PathVariable String token) {
        return ResponseEntity.ok(questionarioService.listarRespostas(token));
    }

    @Operation(summary = "Professor visualiza estatísticas do questionário (RF15)")
    @GetMapping("/questionarios/{id}/estatisticas")
    public ResponseEntity<com.sistema.questionarios.dto.EstatisticaDTO> buscarEstatisticas(@PathVariable Long id) {
        return ResponseEntity.ok(questionarioService.calcularEstatisticas(id));
    }
}
