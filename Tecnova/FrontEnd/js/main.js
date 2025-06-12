// Aguarda o carregamento completo do DOM antes de executar o script
document.addEventListener('DOMContentLoaded', () => {

    const API_BASE_URL = 'http://localhost:8080/api/produtos';

    // Mapeamento de todos os elementos da interface para fácil acesso
    const elements = {
        navLinks: document.querySelectorAll('nav a[href="#"]'), // Apenas os links de SPA
        sections: document.querySelectorAll('main > section'),
        productsContainer: document.getElementById('products-container'),
        searchInput: document.getElementById('search-input'),
        searchBtn: document.getElementById('search-btn'),
        addProductBtn: document.getElementById('add-product-btn'),
        productModal: document.getElementById('product-modal'),
        productForm: document.getElementById('product-form'),
        modalTitle: document.getElementById('modal-title'),
        closeModalBtn: document.querySelector('#product-modal .close-btn'),
        cancelBtn: document.getElementById('cancel-btn'),
        confirmModal: document.getElementById('confirm-modal'),
        confirmOk: document.getElementById('confirm-ok'),
        confirmCancel: document.getElementById('confirm-cancel'),
        addImageBtn: document.getElementById('add-image-btn'),
        imagesPreviewContainer: document.getElementById('product-images-preview'),
        productIdInput: document.getElementById('product-id'),
        productNameInput: document.getElementById('product-name'),
        productDescriptionInput: document.getElementById('product-description'),
        productManufacturerInput: document.getElementById('product-manufacturer'),
        productColorInput: document.getElementById('product-color'),
        productPriceInput: document.getElementById('product-price'),
        productQuantityInput: document.getElementById('product-quantity'),
    };

    // Variáveis de estado
    let newImagesToUpload = [];
    let productToDeleteId = null;

    // --- FUNÇÕES DE LÓGICA (Comunicação com API e UI) ---

    /**
     * Função genérica e robusta para fazer requisições à API.
     */
    const apiRequest = async (url, options = {}) => {
        try {
            const response = await fetch(url, options);
            if (!response.ok) {
                const errorBody = await response.text();
                throw new Error(`Erro ${response.status}: ${errorBody || response.statusText}`);
            }
            if (response.status === 204 || response.headers.get("content-length") === "0") {
                return null;
            }
            return await response.json();
        } catch (error) {
            console.error('Falha na requisição API:', error);
            showError(error.message);
            throw error;
        }
    };
    
    /**
     * Exibe os produtos na tela. É AQUI QUE OS BOTÕES DE EDITAR E EXCLUIR SÃO CRIADOS.
     */
    const displayProducts = (products) => {
        if (!products || products.length === 0) {
            elements.productsContainer.innerHTML = '<div class="no-results">Nenhum produto encontrado.</div>';
            return;
        }

        elements.productsContainer.innerHTML = '';
        products.forEach(product => {
            // Usa a primeira imagem da lista ou uma imagem padrão
            const mainImage = product.imagens && product.imagens.length > 0
                ? product.imagens[0]
                : 'images/placeholder.png'; 

            const productCard = document.createElement('div');
            productCard.className = 'product-card';
            productCard.innerHTML = `
                <div class="product-image-container">
                    <img src="${mainImage}" alt="${product.nome}" class="product-image" onerror="this.src='images/placeholder.png'">
                </div>
                <div class="product-info">
                    <h3>${product.nome}</h3>
                    <p class="product-description">${product.textoDescritivo}</p>
                    <div class="product-details">
                        <span class="manufacturer">${product.fabricante}</span>
                        <span class="color">${product.cor}</span>
                    </div>
                    <div class="product-footer">
                        <span class="price">R$ ${product.preco.toFixed(2).replace('.', ',')}</span>
                        <span class="quantity">${product.quantidade} em estoque</span>
                    </div>
                </div>
                <div class="product-actions">
                    <!-- BOTÃO EDITAR (CRIADO DINAMICAMENTE) -->
                    <button class="btn-edit" data-id="${product.id}"><i class="fas fa-edit"></i> Editar</button>
                    <!-- BOTÃO APAGAR (CRIADO DINAMICAMENTE) -->
                    <button class="btn-delete" data-id="${product.id}"><i class="fas fa-trash"></i> Excluir</button>
                </div>
            `;
            elements.productsContainer.appendChild(productCard);
        });
    };

    /**
     * Carrega os produtos (todos ou por busca) e os exibe.
     */
    const loadProducts = async (searchTerm = null) => {
        elements.productsContainer.innerHTML = '<div class="loading">Carregando produtos...</div>';
        try {
            const url = searchTerm ? `${API_BASE_URL}/search?nome=${encodeURIComponent(searchTerm)}` : API_BASE_URL;
            const products = await apiRequest(url);
            displayProducts(products);
        } catch (error) {
            // O erro já é tratado e exibido na tela pela função apiRequest
        }
    };

    /**
     * Lida com o envio do formulário para CRIAR ou ATUALIZAR um produto.
     */
    const handleFormSubmit = async (e) => {
        e.preventDefault();
        const id = elements.productIdInput.value;
        
        const productJson = {
            nome: elements.productNameInput.value,
            textoDescritivo: elements.productDescriptionInput.value,
            fabricante: elements.productManufacturerInput.value,
            cor: elements.productColorInput.value,
            preco: parseFloat(elements.productPriceInput.value),
            quantidade: parseInt(elements.productQuantityInput.value)
        };

        const formData = new FormData();
        formData.append('produto', new Blob([JSON.stringify(productJson)], { type: 'application/json' }));
        
        const imageKey = id ? 'novasImagens' : 'imagens';
        newImagesToUpload.forEach(file => {
            formData.append(imageKey, file);
        });

        try {
            const url = id ? `${API_BASE_URL}/${id}` : API_BASE_URL;
            const method = id ? 'PUT' : 'POST';
            await apiRequest(url, { method, body: formData });
            
            closeModal();
            loadProducts();
            alert(`Produto ${id ? 'atualizado' : 'criado'} com sucesso!`);
        } catch (error) {
            alert(`Falha ao salvar produto: ${error.message}`);
        }
    };

    /**
     * Lida com a exclusão de um produto após confirmação.
     */
    const handleConfirmDelete = async () => {
        if (!productToDeleteId) return;
        try {
            await apiRequest(`${API_BASE_URL}/${productToDeleteId}`, { method: 'DELETE' });
            loadProducts();
            alert('Produto excluído com sucesso!');
        } catch (error) {
            alert(`Falha ao excluir produto: ${error.message}`);
        } finally {
            closeConfirmDeleteModal();
        }
    };


    // --- Funções Auxiliares da UI ---
    const showError = (message) => {
        elements.productsContainer.innerHTML = `<div class="error"><span>Falha ao carregar</span><small>${message}</small></div>`;
    };
    const handleNavigation = (e) => {
        e.preventDefault();
        const link = e.currentTarget;
        elements.navLinks.forEach(l => l.classList.remove('active'));
        elements.sections.forEach(s => s.classList.remove('active-section'));
        link.classList.add('active');
        const sectionId = link.id.replace('nav-', '') + '-section';
        document.getElementById(sectionId)?.classList.add('active-section');
    };
    const openProductModal = async (productId = null) => {
        elements.productForm.reset();
        elements.imagesPreviewContainer.innerHTML = '';
        newImagesToUpload = [];
        elements.productIdInput.value = '';

        if (productId) {
            elements.modalTitle.textContent = 'Editar Produto';
            try {
                const product = await apiRequest(`${API_BASE_URL}/${productId}`);
                elements.productIdInput.value = product.id;
                elements.productNameInput.value = product.nome;
                elements.productDescriptionInput.value = product.textoDescritivo;
                // ... preencher outros campos
                elements.productManufacturerInput.value = product.fabricante;
                elements.productColorInput.value = product.cor;
                elements.productPriceInput.value = product.preco;
                elements.productQuantityInput.value = product.quantidade;
            } catch (error) {
                alert('Erro ao carregar dados do produto para edição.');
                return;
            }
        } else {
            elements.modalTitle.textContent = 'Adicionar Novo Produto';
        }
        elements.productModal.style.display = 'flex';
    };
    const closeModal = () => elements.productModal.style.display = 'none';
    const handleAddImageClick = () => {
        const fileInput = document.createElement('input');
        fileInput.type = 'file';
        fileInput.accept = 'image/*';
        fileInput.multiple = true;
        fileInput.onchange = (e) => {
            Array.from(e.target.files).forEach(file => {
                newImagesToUpload.push(file);
                const reader = new FileReader();
                reader.onload = (event) => {
                    const imgDiv = document.createElement('div');
                    imgDiv.className = 'image-preview new';
                    imgDiv.innerHTML = `<img src="${event.target.result}" alt="Nova imagem"><button type="button" class="btn-remove-image">&times;</button>`;
                    elements.imagesPreviewContainer.appendChild(imgDiv);
                    imgDiv.querySelector('.btn-remove-image').addEventListener('click', () => {
                        newImagesToUpload = newImagesToUpload.filter(f => f !== file);
                        imgDiv.remove();
                    });
                };
                reader.readAsDataURL(file);
            });
        };
        fileInput.click();
    };
    const openConfirmDeleteModal = (id) => {
        productToDeleteId = id;
        elements.confirmModal.style.display = 'flex';
    };
    const closeConfirmDeleteModal = () => {
        productToDeleteId = null;
        elements.confirmModal.style.display = 'none';
    };
    
    // --- LIGAÇÃO DOS EVENTOS (EVENT LISTENERS) ---
    
    // FUNCIONALIDADE DE NAVEGAÇÃO
    elements.navLinks.forEach(link => link.addEventListener('click', handleNavigation));

    // FUNCIONALIDADE DE BUSCA
    elements.searchBtn.addEventListener('click', () => loadProducts(elements.searchInput.value.trim()));
    elements.searchInput.addEventListener('keypress', (e) => e.key === 'Enter' && loadProducts(elements.searchInput.value.trim()));

    // FUNCIONALIDADE ADICIONAR
    elements.addProductBtn.addEventListener('click', () => openProductModal());
    
    // Ações do Modal de Produto (CRIAR/ATUALIZAR)
    elements.closeModalBtn.addEventListener('click', closeModal);
    elements.cancelBtn.addEventListener('click', closeModal);
    elements.productForm.addEventListener('submit', handleFormSubmit);
    elements.addImageBtn.addEventListener('click', handleAddImageClick);

    // Delegação de eventos para botões de EDITAR e APAGAR
    elements.productsContainer.addEventListener('click', (e) => {
        const editBtn = e.target.closest('.btn-edit');
        const deleteBtn = e.target.closest('.btn-delete');
        if (editBtn) openProductModal(editBtn.dataset.id);
        if (deleteBtn) openConfirmDeleteModal(deleteBtn.dataset.id);
    });

    // Ações do Modal de Confirmação (APAGAR)
    elements.confirmOk.addEventListener('click', handleConfirmDelete);
    elements.confirmCancel.addEventListener('click', closeConfirmDeleteModal);
    
    // --- Carga Inicial ---
    loadProducts();
});
