const apiUrl = 'http://localhost:8080/api/produtos'; // ajuste conforme backend

const form = document.getElementById('formProduto');
const produtoIdInput = document.getElementById('produtoId');
const nomeInput = document.getElementById('nome');
const descricaoInput = document.getElementById('descricao');
const precoInput = document.getElementById('preco');
const quantidadeInput = document.getElementById('quantidade');
const imagemInput = document.getElementById('imagem');
const filtroNomeInput = document.getElementById('filtroNome');
const listaProdutosDiv = document.getElementById('listaProdutos');

function limparFormulario() {
  produtoIdInput.value = '';
  nomeInput.value = '';
  descricaoInput.value = '';
  precoInput.value = '';
  quantidadeInput.value = '';
  imagemInput.value = '';
}

document.getElementById('limparBtn').addEventListener('click', limparFormulario);

function mostrarProdutos(produtos) {
  listaProdutosDiv.innerHTML = '';
  if (produtos.length === 0) {
    listaProdutosDiv.textContent = 'Nenhum produto encontrado.';
    return;
  }
  produtos.forEach(p => {
    const div = document.createElement('div');
    div.className = 'produto';
    div.innerHTML = `
      <strong>${p.nome}</strong> (R$ ${p.preco.toFixed(2)})<br/>
      Quantidade: ${p.quantidade}<br/>
      ${p.descricao ? `<p>${p.descricao}</p>` : ''}
      ${p.imagem ? `<img src="${p.imagem}" alt="Imagem do produto" />` : ''}
      <button data-id="${p.id}" class="btnEditar">Editar</button>
      <button data-id="${p.id}" class="btnExcluir">Excluir</button>
    `;
    listaProdutosDiv.appendChild(div);
  });

  document.querySelectorAll('.btnEditar').forEach(btn => {
    btn.addEventListener('click', e => {
      const id = e.target.getAttribute('data-id');
      buscarProdutoPorId(id);
    });
  });

  document.querySelectorAll('.btnExcluir').forEach(btn => {
    btn.addEventListener('click', e => {
      const id = e.target.getAttribute('data-id');
      if (confirm('Deseja realmente excluir este produto?')) {
        excluirProduto(id);
      }
    });
  });
}

function buscarProdutoPorId(id) {
  fetch(`${apiUrl}/${id}`)
    .then(res => {
      if (!res.ok) throw new Error('Produto não encontrado');
      return res.json();
    })
    .then(p => {
      produtoIdInput.value = p.id;
      nomeInput.value = p.nome;
      descricaoInput.value = p.descricao || '';
      precoInput.value = p.preco;
      quantidadeInput.value = p.quantidade;
      if(p.imagem) {
        alert('Imagem atual existe. Para alterar, escolha um novo arquivo.');
      }
    })
    .catch(err => alert(err.message));
}

function excluirProduto(id) {
  fetch(`${apiUrl}/${id}`, { method: 'DELETE' })
    .then(res => {
      if (res.status === 204) {
        alert('Produto excluído com sucesso.');
        buscarProdutos();
        limparFormulario();
      } else {
        throw new Error('Erro ao excluir produto');
      }
    })
    .catch(err => alert(err.message));
}

function buscarProdutos() {
  const nome = filtroNomeInput.value.trim();
  let url = apiUrl;
  if (nome) {
    url += `?nome=${encodeURIComponent(nome)}`;
  }
  fetch(url)
    .then(res => res.json())
    .then(produtos => mostrarProdutos(produtos))
    .catch(err => alert('Erro ao buscar produtos: ' + err.message));
}

document.getElementById('btnBuscar').addEventListener('click', buscarProdutos);
document.getElementById('btnListar').addEventListener('click', () => {
  filtroNomeInput.value = '';
  buscarProdutos();
});

form.addEventListener('submit', e => {
  e.preventDefault();

  const id = produtoIdInput.value;
  const formData = new FormData();

  const produto = {
    nome: nomeInput.value.trim(),
    descricao: descricaoInput.value.trim(),
    preco: parseFloat(precoInput.value),
    quantidade: parseInt(quantidadeInput.value),
    imagem: null
  };

  if(id) produto.id = parseInt(id);

  formData.append('produto', JSON.stringify(produto));

  if (imagemInput.files.length > 0) {
    formData.append('imagem', imagemInput.files[0]);
  }

  let method = 'POST';
  let url = apiUrl;

  if (id) {
    method = 'PUT';
    url += '/' + id;
  }

  fetch(url, {
    method,
    body: formData,
  })
    .then(res => {
      if (!res.ok) {
        return res.json().then(data => Promise.reject(data.message || 'Erro na requisição'));
      }
      return res.json();
    })
    .then(data => {
      alert('Produto salvo com sucesso!');
      limparFormulario();
      buscarProdutos();
    })
    .catch(err => alert('Erro: ' + err));
});

// Carrega lista inicial
buscarProdutos();
