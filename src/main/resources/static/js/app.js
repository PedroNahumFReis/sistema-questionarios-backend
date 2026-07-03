/* ============================================================
   app.js — lógica do painel do PROFESSOR.
   Conversa com a API REST via fetch. O token JWT fica no localStorage.
   ============================================================ */

const API = "/api";

// Guarda/recupera o token JWT do navegador (mantém o login entre reloads).
const auth = {
  get token() { return localStorage.getItem("jwt"); },
  set token(v) { v ? localStorage.setItem("jwt", v) : localStorage.removeItem("jwt"); },
};

// Wrapper de fetch que já injeta o cabeçalho Authorization e trata erros.
async function api(path, { method = "GET", body, semAuth = false } = {}) {
  const headers = { "Content-Type": "application/json" };
  if (!semAuth && auth.token) headers["Authorization"] = "Bearer " + auth.token;

  const res = await fetch(API + path, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  // 401/403: sessão inválida -> volta para o login.
  if (res.status === 401 || res.status === 403) {
    auth.token = null;
    mostrarLogin();
    throw new Error("Sessão expirada. Entre novamente.");
  }

  const texto = await res.text();
  const dado = texto ? JSON.parse(texto) : null;
  if (!res.ok) throw new Error(dado?.erro || "Erro na requisição (" + res.status + ").");
  return dado;
}

// Helpers de UI
const $ = (id) => document.getElementById(id);
function alerta(elId, msg, tipo = "error") {
  $(elId).innerHTML = msg ? `<div class="alert alert-${tipo}">${msg}</div>` : "";
}
function escapeHtml(s) {
  return String(s ?? "").replace(/[&<>"']/g, (c) =>
    ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

/* ---------------- Autenticação ---------------- */
function mostrarLogin() {
  $("appView").classList.add("hidden");
  $("loginView").classList.remove("hidden");
  $("btnLogout").classList.add("hidden");
}
function mostrarApp() {
  $("loginView").classList.add("hidden");
  $("appView").classList.remove("hidden");
  $("btnLogout").classList.remove("hidden");
  carregarTudo();
}

$("btnLogin").onclick = async () => {
  alerta("loginAlert", "");
  try {
    const dado = await api("/auth/login", {
      method: "POST",
      semAuth: true,
      body: { email: $("loginEmail").value.trim(), senha: $("loginSenha").value },
    });
    auth.token = dado.token;
    mostrarApp();
  } catch (e) {
    alerta("loginAlert", e.message);
  }
};

$("btnShowCadastro").onclick = () => $("cadastroCard").classList.toggle("hidden");

$("btnCadastrar").onclick = async () => {
  alerta("cadastroAlert", "");
  try {
    await api("/professores", {
      method: "POST",
      semAuth: true,
      body: { nome: $("cadNome").value.trim(), email: $("cadEmail").value.trim(), senha: $("cadSenha").value },
    });
    alerta("cadastroAlert", "Conta criada! Agora é só entrar.", "success");
  } catch (e) {
    alerta("cadastroAlert", e.message);
  }
};

$("btnLogout").onclick = () => { auth.token = null; mostrarLogin(); };

/* ---------------- Abas ---------------- */
document.querySelectorAll(".tab").forEach((tab) => {
  tab.onclick = () => {
    document.querySelectorAll(".tab").forEach((t) => t.classList.remove("active"));
    tab.classList.add("active");
    document.querySelectorAll("#appView section").forEach((s) => s.classList.add("hidden"));
    $("tab-" + tab.dataset.tab).classList.remove("hidden");
  };
});

/* ---------------- Construtor de perguntas ---------------- */
function novaPerguntaBox() {
  const box = document.createElement("div");
  box.className = "pergunta-box";
  box.innerHTML = `
    <div class="ph">
      <strong>Pergunta</strong>
      <button class="btn btn-danger-ghost btn-sm" type="button" data-remove-pergunta>Remover</button>
    </div>
    <label>Enunciado</label>
    <input type="text" class="p-enunciado" placeholder="Quanto é 2 + 2?" />
    <div class="row">
      <div>
        <label>Tipo</label>
        <select class="p-tipo">
          <option value="UNICA_ESCOLHA">Única escolha</option>
          <option value="MULTIPLA_ESCOLHA">Múltipla escolha</option>
          <option value="VERDADEIRO_FALSO">Verdadeiro/Falso</option>
        </select>
      </div>
      <div>
        <label>Pontuação</label>
        <input type="number" class="p-pontuacao" value="1" min="0" step="0.5" />
      </div>
    </div>
    <label class="mt">Alternativas <span class="muted">(marque a(s) correta(s))</span></label>
    <div class="alts"></div>
    <button class="btn btn-ghost btn-sm mt" type="button" data-add-alt>+ Alternativa</button>
  `;
  const alts = box.querySelector(".alts");
  const addAlt = () => alts.appendChild(novaAltRow());
  addAlt(); addAlt(); // começa com 2 alternativas

  box.querySelector("[data-add-alt]").onclick = addAlt;
  box.querySelector("[data-remove-pergunta]").onclick = () => box.remove();
  return box;
}

function novaAltRow() {
  const row = document.createElement("div");
  row.className = "alt-row";
  row.innerHTML = `
    <input type="text" class="a-texto" placeholder="Texto da alternativa" />
    <label class="correct-wrap"><input type="checkbox" class="a-correta" /> correta</label>
    <button class="btn btn-danger-ghost btn-sm" type="button" data-remove-alt>✕</button>
  `;
  row.querySelector("[data-remove-alt]").onclick = () => row.remove();
  return row;
}

$("btnAddPergunta").onclick = () => $("perguntasContainer").appendChild(novaPerguntaBox());

// Lê o formulário e monta o objeto do questionário no formato que a API espera.
function coletarQuestionario() {
  const perguntas = [...document.querySelectorAll("#perguntasContainer .pergunta-box")].map((box) => ({
    enunciado: box.querySelector(".p-enunciado").value.trim(),
    tipo: box.querySelector(".p-tipo").value,
    pontuacao: parseFloat(box.querySelector(".p-pontuacao").value) || 0,
    alternativas: [...box.querySelectorAll(".alt-row")].map((r) => ({
      texto: r.querySelector(".a-texto").value.trim(),
      correta: r.querySelector(".a-correta").checked,
    })),
  }));
  return {
    titulo: $("qTitulo").value.trim(),
    descricao: $("qDescricao").value.trim(),
    tempoValidadeTokenMinutos: parseInt($("qValidade").value) || 60,
    perguntas,
  };
}

$("btnSalvarQuestionario").onclick = async () => {
  alerta("qAlert", "");
  const q = coletarQuestionario();
  if (!q.titulo) return alerta("qAlert", "Informe o título.");
  if (q.perguntas.length === 0) return alerta("qAlert", "Adicione ao menos uma pergunta.");
  try {
    await api("/questionarios", { method: "POST", body: q });
    alerta("qAlert", "Questionário criado com sucesso!", "success");
    $("qTitulo").value = ""; $("qDescricao").value = "";
    $("perguntasContainer").innerHTML = "";
    carregarQuestionarios();
  } catch (e) {
    alerta("qAlert", e.message);
  }
};

/* ---------------- Listagens ---------------- */
async function carregarQuestionarios() {
  try {
    const lista = await api("/questionarios");
    // Preenche a lista visual
    $("listaQuestionarios").innerHTML = lista.length
      ? lista.map((q) => `
        <div class="list-item">
          <div>
            <strong>${escapeHtml(q.titulo)}</strong>
            <div class="meta">${q.perguntas ? q.perguntas.length : 0} pergunta(s) · validade ${q.tempoValidadeTokenMinutos ?? "?"} min</div>
          </div>
          <span class="pill">#${q.id}</span>
        </div>`).join("")
      : `<p class="muted">Nenhum questionário ainda.</p>`;

    // Preenche os selects das abas Enviar e Estatísticas.
    const options = lista.map((q) => `<option value="${q.id}">#${q.id} — ${escapeHtml(q.titulo)}</option>`).join("");
    $("eQuestionario").innerHTML = options || `<option value="">(nenhum)</option>`;
    $("sQuestionario").innerHTML = options || `<option value="">(nenhum)</option>`;
  } catch (e) {
    $("listaQuestionarios").innerHTML = `<div class="alert alert-error">${e.message}</div>`;
  }
}

async function carregarAlunos() {
  try {
    const lista = await api("/alunos");
    $("listaAlunos").innerHTML = lista.length
      ? lista.map((a) => `
        <div class="list-item">
          <div><strong>${escapeHtml(a.nome)}</strong><div class="meta">${escapeHtml(a.email)} · matrícula ${escapeHtml(a.matricula)}</div></div>
          <span class="pill">#${a.id}</span>
        </div>`).join("")
      : `<p class="muted">Nenhum aluno cadastrado.</p>`;
    $("eAluno").innerHTML = lista.map((a) => `<option value="${a.id}">${escapeHtml(a.nome)} (${escapeHtml(a.email)})</option>`).join("")
      || `<option value="">(nenhum)</option>`;
  } catch (e) {
    $("listaAlunos").innerHTML = `<div class="alert alert-error">${e.message}</div>`;
  }
}

$("btnSalvarAluno").onclick = async () => {
  alerta("aAlert", "");
  try {
    await api("/alunos", {
      method: "POST",
      body: { nome: $("aNome").value.trim(), email: $("aEmail").value.trim(), matricula: $("aMatricula").value.trim() },
    });
    alerta("aAlert", "Aluno cadastrado!", "success");
    $("aNome").value = ""; $("aEmail").value = ""; $("aMatricula").value = "";
    carregarAlunos();
  } catch (e) {
    alerta("aAlert", e.message);
  }
};

/* ---------------- Enviar ---------------- */
$("btnEnviar").onclick = async () => {
  alerta("eAlert", "");
  $("linkGeradoBox").classList.add("hidden");
  const idQ = $("eQuestionario").value, idA = $("eAluno").value;
  if (!idQ || !idA) return alerta("eAlert", "Selecione um questionário e um aluno.");
  try {
    const dado = await api(`/questionarios/${idQ}/enviar/${idA}`, { method: "POST" });
    const link = window.location.origin + "/responder.html?token=" + dado.token;
    $("linkGerado").value = link;
    $("linkAbrir").href = link;
    $("linkGeradoBox").classList.remove("hidden");
  } catch (e) {
    alerta("eAlert", e.message);
  }
};

$("btnCopiar").onclick = () => {
  $("linkGerado").select();
  navigator.clipboard?.writeText($("linkGerado").value);
  $("btnCopiar").textContent = "Copiado!";
  setTimeout(() => ($("btnCopiar").textContent = "Copiar"), 1500);
};

/* ---------------- Estatísticas ---------------- */
$("btnCarregarStats").onclick = async () => {
  alerta("sAlert", "");
  const id = $("sQuestionario").value;
  if (!id) return alerta("sAlert", "Selecione um questionário.");
  try {
    const s = await api(`/questionarios/${id}/estatisticas`);
    $("statsResultado").innerHTML = renderStats(s);
  } catch (e) {
    $("statsResultado").innerHTML = "";
    alerta("sAlert", e.message);
  }
};

function renderStats(s) {
  if (s.totalRespostas === 0) return `<div class="alert alert-info">Ainda não há respostas para este questionário.</div>`;
  const perguntas = (s.porPergunta || []).map((p) => `
    <div class="mt">
      <div style="display:flex;justify-content:space-between;font-size:0.9rem;">
        <span>${escapeHtml(p.enunciado)}</span>
        <strong>${p.percentualAcertos.toFixed(0)}%</strong>
      </div>
      <div class="bar-track"><div class="bar-fill" style="width:${p.percentualAcertos}%"></div></div>
      <div class="meta">${p.respostasCorretas} acerto(s) · ${p.respostasIncorretas} erro(s)</div>
    </div>`).join("");

  return `
    <div class="stat-cards">
      <div class="stat-card"><div class="num">${s.totalRespostas}</div><div class="lbl">Respostas</div></div>
      <div class="stat-card"><div class="num">${s.respostasCorretas}</div><div class="lbl">Acertos</div></div>
      <div class="stat-card"><div class="num">${s.respostasIncorretas}</div><div class="lbl">Erros</div></div>
      <div class="stat-card"><div class="num">${s.percentualAcertos.toFixed(0)}%</div><div class="lbl">Aproveitamento</div></div>
    </div>
    <h2 style="font-size:1rem;">Desempenho por pergunta</h2>
    ${perguntas}`;
}

/* ---------------- Init ---------------- */
function carregarTudo() { carregarQuestionarios(); carregarAlunos(); }

// Ao abrir a página: se já houver token salvo, vai direto para o painel.
if (auth.token) mostrarApp(); else mostrarLogin();
