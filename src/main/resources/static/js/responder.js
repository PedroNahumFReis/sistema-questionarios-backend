/* ============================================================
   responder.js — lógica da página do ALUNO.
   Lê o token da URL (?token=...), carrega o questionário, coleta as respostas
   e envia para correção. Nenhuma rota aqui precisa de login.
   ============================================================ */

const API = "/api";
const $ = (id) => document.getElementById(id);

// Pega o token da query string da URL.
const token = new URLSearchParams(window.location.search).get("token");

function escapeHtml(s) {
  return String(s ?? "").replace(/[&<>"']/g, (c) =>
    ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

function mostrarErro(msg) {
  $("loading").classList.add("hidden");
  $("quizView").classList.add("hidden");
  $("erroView").classList.remove("hidden");
  $("erroMsg").textContent = msg;
}

// Guarda o questionário carregado para saber o tipo de cada pergunta na hora de coletar.
let questionario = null;

/* ---------------- Carregar questionário ---------------- */
async function carregar() {
  if (!token) return mostrarErro("Link inválido: token não encontrado na URL.");
  try {
    const res = await fetch(`${API}/responder/${token}`);
    const texto = await res.text();
    const dado = texto ? JSON.parse(texto) : null;
    if (!res.ok) return mostrarErro(dado?.erro || "Não foi possível carregar o questionário.");

    questionario = dado;
    renderQuiz(dado);
  } catch (e) {
    mostrarErro("Erro de conexão ao carregar o questionário.");
  }
}

function renderQuiz(q) {
  $("loading").classList.add("hidden");
  $("quizView").classList.remove("hidden");
  $("quizTitulo").textContent = q.titulo;
  $("quizDescricao").textContent = q.descricao || "";

  // Múltipla escolha usa checkbox; as demais usam radio (uma escolha por pergunta).
  $("perguntas").innerHTML = q.perguntas.map((p, i) => {
    const multi = p.tipo === "MULTIPLA_ESCOLHA";
    const inputType = multi ? "checkbox" : "radio";
    const opts = p.alternativas.map((a) => `
      <label class="opt">
        <input type="${inputType}" name="p_${p.id}" value="${a.id}" />
        <span>${escapeHtml(a.texto)}</span>
      </label>`).join("");
    return `
      <div class="card">
        <div class="pill">Pergunta ${i + 1}</div>
        <h2 style="font-size:1.05rem;margin-top:10px;">${escapeHtml(p.enunciado)}</h2>
        ${opts}
      </div>`;
  }).join("");
}

/* ---------------- Enviar respostas ---------------- */
$("btnEnviarRespostas").onclick = async () => {
  $("quizAlert").innerHTML = "";

  // Coleta todos os inputs marcados (radio ou checkbox) de todas as perguntas.
  const marcados = [...document.querySelectorAll('#perguntas input:checked')].map((el) => parseInt(el.value));
  if (marcados.length === 0) {
    $("quizAlert").innerHTML = `<div class="alert alert-error">Selecione ao menos uma alternativa.</div>`;
    return;
  }

  $("btnEnviarRespostas").disabled = true;
  try {
    const res = await fetch(`${API}/responder/${token}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ idsAlternativasSelecionadas: marcados }),
    });
    const texto = await res.text();
    const dado = texto ? JSON.parse(texto) : null;
    if (!res.ok) throw new Error(dado?.erro || "Erro ao enviar respostas.");
    mostrarResultado(dado);
  } catch (e) {
    $("btnEnviarRespostas").disabled = false;
    $("quizAlert").innerHTML = `<div class="alert alert-error">${e.message}</div>`;
  }
};

function mostrarResultado(r) {
  $("quizView").classList.add("hidden");
  $("resultView").classList.remove("hidden");
  $("rGrade").textContent = r.notaFinal;
  $("rAcertos").textContent = r.acertos;
  $("rErros").textContent = r.erros;
  $("rPerc").textContent = r.percentual.toFixed(0) + "%";
}

/* ---------------- Ver respostas (RF14) ---------------- */
$("btnVerRespostas").onclick = async () => {
  try {
    const res = await fetch(`${API}/respostas/${token}`);
    const lista = await res.json();
    $("respostasDetalhe").innerHTML = lista.map((r) => `
      <div class="list-item">
        <div>
          <strong>${escapeHtml(r.enunciado)}</strong>
          <div class="meta">Você marcou: ${escapeHtml(r.textoAlternativa)}</div>
        </div>
        <span class="pill" style="background:${r.correta ? "var(--success-soft)" : "var(--danger-soft)"};color:${r.correta ? "var(--success)" : "var(--danger)"};">
          ${r.correta ? "✓ correta" : "✕ errada"}
        </span>
      </div>`).join("");
    $("btnVerRespostas").classList.add("hidden");
  } catch (e) {
    $("respostasDetalhe").innerHTML = `<div class="alert alert-error">Não foi possível carregar as respostas.</div>`;
  }
};

// Inicia
carregar();
