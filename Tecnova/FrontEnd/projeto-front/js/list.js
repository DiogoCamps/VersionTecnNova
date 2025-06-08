const tabela = document.querySelector("#tabela-produtos tbody");
const inputBusca = document.getElementById("input-busca");
const btnNovo = document.getElementById("btn-novo");

const API_URL = "http://localhost:8080/api/produtos";

function criarLinha(produto) {
  const tr = document.createElement("tr");

  tr.innerHTML = `
    <td>${produto.nome}</td>
    <td>${produto.preco.toFixed(2)}</td>
    <td>${produto.quantidade}</td>
    <td>${produto.imagem ? `<img src="${produto.imagem}" alt="${produto.nome}" class="produto-img" />` : "-"}</td>
    <td>
      <button class="btn btn-green btn-editar" data-id="${produto.id}">Editar</button>
      <button class="btn btn-gray btn-remover" data-id="${produto.id}">Remover</button>
    </td>
  `;

  return tr;
}

async function carregarProdutos(filtro = "") {
  let url = API_URL;
  if (filtro) {
    url += `/buscar?nome=${encodeURIComponent(filtro)}`;
  }

  const res = await fetch(url);
  if (!res.ok) {
    alert("Erro ao carregar produtos");
    return;
  }

  const produtos = await res.json();

  tabela.innerHTML = "";
  produtos.forEach(produto => {
    tabela.appendChild(criarLinha(produto));
  });

  // Adiciona eventos nos botões depois de criar as linhas
  document.querySelectorAll(".btn-editar").forEach(btn => {
    btn.onclick = e => {
      const id = e.target.dataset.id;
      window.location.href = `form.html?id=${id}`;
    };
  });

  document.querySelectorAll(".btn-remover").forEach(btn => {
    btn.onclick = async e => {
      const id = e.target.dataset.id;
      if (confirm("Deseja realmente remover este produto?")) {
        const res = await fetch(`${API_URL}/${id}`, { method: "DELETE" });
        if (res.ok) {
          alert("Produto removido");
          carregarProdutos(inputBusca.value);
        } else {
          alert("Erro ao remover produto");
        }
      }
    };
  });
}

btnNovo.onclick = () => {
  window.location.href = "form.html";
};

inputBusca.oninput = () => {
  carregarProdutos(inputBusca.value.trim());
};

window.onload = () => {
  carregarProdutos();
};
