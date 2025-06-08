const form = document.getElementById("form-produto");
const tituloForm = document.getElementById("titulo-form");
const inputId = document.getElementById("produto-id");
const inputNome = document.getElementById("nome");
const inputDescricao = document.getElementById("descricao");
const inputPreco = document.getElementById("preco");
const inputQuantidade = document.getElementById("quantidade");
const inputImagem = document.getElementById("imagem");
const previewImagem = document.getElementById("preview-imagem");

const API_URL = "http://localhost:8080/api/produtos";

let imagemBase64 = null;

function urlParams() {
  return new URLSearchParams(window.location.search);
}

// Exibe preview da imagem ao selecionar
inputImagem.onchange = () => {
  const file = inputImagem.files[0];
  if (file) {
    const reader = new FileReader();
    reader.onload = () => {
      imagemBase64 = reader.result; // base64 da imagem para enviar
      previewImagem.innerHTML = `<img src="${imagemBase64}" alt="Preview" />`;
    };
    reader.readAsDataURL(file);
  } else {
    imagemBase64 = null;
    previewImagem.innerHTML = "";
  }
};

// Carregar produto para edição se id existir
async function carregarProduto(id) {
  const res = await fetch(`${API_URL}/${id}`);
  if (!res.ok) {
    alert("Produto não encontrado");
    window.location.href = "index.html";
    return;
  }

  const produto = await res.json();
  inputId.value = produto.id;
  inputNome.value = produto.nome;
  inputDescricao.value = produto.descricao || "";
  inputPreco.value = produto.preco;
  inputQuantidade.value = produto.quantidade;
  imagemBase64 = produto.imagem || null;

  if (imagemBase64) {
    previewImagem.innerHTML = `<img src="${imagemBase64}" alt="Preview" />`;
  }

  tituloForm.textContent = "Editar Produto";
}

// Enviar formulário para criar ou atualizar
form.onsubmit = async e => {
  e.preventDefault();

  const id = inputId.value.trim();
  const data = {
    nome: inputNome.value.trim(),
    descricao: inputDescricao.value.trim(),
    preco: parseFloat(inputPreco.value),
    quantidade: parseInt(inputQuantidade.value),
    imagem: imagemBase64,
  };

  // Validações básicas
  if (!data.nome || isNaN(data.preco) || isNaN(data.quantidade)) {
    alert("Preencha os campos obrigatórios corretamente.");
    return;
  }

  try {
    let res;
    if (id) {
      res = await fetch(`${API_URL}/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });
    } else {
      res = await fetch(API_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });
    }

    if (res.ok) {
      alert("Produto salvo com sucesso!");
      window.location.href = "index.html";
    } else {
      alert("Erro ao salvar produto");
    }
  } catch (err) {
    alert("Erro na requisição: " + err.message);
  }
};

document.getElementById("btn-cancelar").onclick = () => {
  window.location.href = "index.html";
};

window.onload = () => {
  const params = urlParams();
  if (params.has("id")) {
    carregarProduto(params.get("id"));
  }
};
