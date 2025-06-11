// ProductService - Serviço para comunicação com a API
class ProductService {
    constructor(baseUrl = 'http://localhost:8080/api/produtos') {
        this.baseUrl = baseUrl;
    }

    /**
     * Função auxiliar para realizar requisições HTTP e tratar erros.
     * @param {string} url - A URL para a requisição.
     * @param {Object} options - Opções da requisição (método, headers, body, etc.).
     * @returns {Promise<Object>} - Os dados da resposta JSON.
     * @throws {Error} - Lança um erro em caso de falha na requisição.
     */
    async #fetchData(url, options = {}) {
        try {
            const response = await fetch(url, options);
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({ message: 'Erro desconhecido' }));
                throw new Error(errorData.message || `Erro na requisição: ${response.status} ${response.statusText}`);
            }
            return await response.json();
        } catch (error) {
            console.error('Erro no ProductService:', error);
            throw error; // Propaga o erro para ser tratado no UIController
        }
    }

    /**
     * Obtém todos os produtos.
     * @returns {Promise<Array>} - Uma lista de produtos.
     */
    getAllProducts() {
        return this.#fetchData(this.baseUrl);
    }

    /**
     * Obtém um produto pelo ID.
     * @param {string} id - O ID do produto.
     * @returns {Promise<Object>} - O objeto produto.
     */
    getProductById(id) {
        return this.#fetchData(`${this.baseUrl}/${id}`);
    }

    /**
     * Busca produtos pelo nome.
     * @param {string} name - O termo de busca.
     * @returns {Promise<Array>} - Uma lista de produtos que correspondem ao nome.
     */
    searchProductsByName(name) {
        return this.#fetchData(`${this.baseUrl}/search?nome=${encodeURIComponent(name)}`);
    }

    /**
     * Cria um novo produto com imagens.
     * @param {Object} productData - Dados do produto.
     * @param {File[]} productData.imagens - Array de objetos File das imagens a serem enviadas.
     * @returns {Promise<Object>} - O produto criado.
     */
    createProduct(productData) {
        const formData = this.#buildProductFormData(productData, productData.imagens);
        return this.#fetchData(this.baseUrl, {
            method: 'POST',
            body: formData
        });
    }

    /**
     * Atualiza um produto existente com novas imagens.
     * @param {string} id - O ID do produto a ser atualizado.
     * @param {Object} productData - Dados do produto atualizados.
     * @param {File[]} productData.novasImagens - Array de objetos File de novas imagens a serem adicionadas.
     * @returns {Promise<Object>} - O produto atualizado.
     */
    updateProduct(id, productData) {
        // Envia apenas novas imagens (se houver). O backend deve gerenciar as imagens existentes.
        const formData = this.#buildProductFormData(productData, productData.novasImagens);
        return this.#fetchData(`${this.baseUrl}/${id}`, {
            method: 'PUT',
            body: formData
        });
    }

    /**
     * Exclui um produto.
     * @param {string} id - O ID do produto a ser excluído.
     * @returns {Promise<boolean>} - True se a exclusão for bem-sucedida.
     */
    deleteProduct(id) {
        return this.#fetchData(`${this.baseUrl}/${id}`, {
            method: 'DELETE'
        });
    }

    /**
     * Constrói o FormData para envio de dados do produto e imagens.
     * @param {Object} productData - Dados do produto.
     * @param {File[]} [images=[]] - Array de objetos File das imagens.
     * @returns {FormData} - O objeto FormData.
     */
    #buildProductFormData(productData, images = []) {
        const formData = new FormData();
        formData.append('produto', new Blob([JSON.stringify({
            nome: productData.nome,
            textoDescritivo: productData.textoDescritivo,
            cor: productData.cor,
            fabricante: productData.fabricante,
            preco: productData.preco,
            quantidade: productData.quantidade
        })], { type: 'application/json' }));

        images.forEach(file => {
            formData.append('imagens', file); // 'imagens' deve corresponder ao @RequestParam no Controller Spring
        });
        return formData;
    }
}

const productService = new ProductService();

// UI Controller - Controlador principal da interface
class UIController {
    constructor() {
        this.elements = {};
        this.currentProductId = null;
        this.newImagesToUpload = []; // Armazena os objetos File das novas imagens para upload

        this.#initializeElements();
        this.#setupEventListeners();
        this.#loadInitialData();
    }

    /**
     * Inicializa as referências aos elementos DOM.
     * Usando 'this.elements' para centralizar e facilitar o acesso.
     */
    #initializeElements() {
        this.elements = {
            navLinks: document.querySelectorAll('nav a'),
            sections: document.querySelectorAll('main section'),
            homeSection: document.getElementById('home-section'),
            productsSection: document.getElementById('products-section'),
            productsContainer: document.getElementById('products-container'),
            searchInput: document.getElementById('search-input'),
            searchBtn: document.getElementById('search-btn'),
            addProductBtn: document.getElementById('add-product-btn'),
            productModal: document.getElementById('product-modal'),
            productForm: document.getElementById('product-form'),
            modalTitle: document.getElementById('modal-title'),
            closeModalBtn: document.querySelector('.close-btn'),
            cancelBtn: document.getElementById('cancel-btn'),
            confirmModal: document.getElementById('confirm-modal'),
            confirmOk: document.getElementById('confirm-ok'),
            confirmCancel: document.getElementById('confirm-cancel'),
            addImageBtn: document.getElementById('add-image-btn'),
            productImagesContainer: document.getElementById('product-images-container'),
            productIdInput: document.getElementById('product-id'),
            productNameInput: document.getElementById('product-name'),
            productDescriptionInput: document.getElementById('product-description'),
            productManufacturerInput: document.getElementById('product-manufacturer'),
            productColorInput: document.getElementById('product-color'),
            productPriceInput: document.getElementById('product-price'),
            productQuantityInput: document.getElementById('product-quantity'),
            manufacturerChartCtx: document.getElementById('manufacturer-chart')?.getContext('2d'),
            colorChartCtx: document.getElementById('color-chart')?.getContext('2d'),
            stockChartCtx: document.getElementById('stock-chart')?.getContext('2d')
        };
    }

    /**
     * Configura os event listeners para os elementos da UI.
     */
    #setupEventListeners() {
        // Navegação
        this.elements.navLinks.forEach(link => {
            link.addEventListener('click', (e) => this.#handleNavigation(e, link));
        });

        // Busca
        this.elements.searchBtn.addEventListener('click', () => this.#handleSearch());
        this.elements.searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.#handleSearch();
        });

        // Modal de produto
        this.elements.addProductBtn.addEventListener('click', () => this.#openProductModal());
        this.elements.closeModalBtn.addEventListener('click', () => this.#closeModal());
        this.elements.cancelBtn.addEventListener('click', () => this.#closeModal());
        this.elements.addImageBtn.addEventListener('click', () => this.#addImageField());
        this.elements.productForm.addEventListener('submit', (e) => this.#handleFormSubmit(e));

        // Modal de confirmação
        this.elements.confirmCancel.addEventListener('click', () => this.#closeConfirmModal());
        this.elements.confirmOk.addEventListener('click', () => this.#handleConfirmAction());
    }

    /**
     * Carrega os dados iniciais ao iniciar a aplicação.
     */
    #loadInitialData() {
        this.#loadProducts();
        this.#loadCharts();
    }

    /**
     * Manipula a navegação entre as seções da página.
     * @param {Event} e - O evento de clique.
     * @param {HTMLElement} link - O link de navegação clicado.
     */
    #handleNavigation(e, link) {
        e.preventDefault();
        
        // Remove 'active' de todos os links e seções
        this.elements.navLinks.forEach(navLink => navLink.classList.remove('active'));
        this.elements.sections.forEach(section => section.classList.remove('active-section'));
        
        // Adiciona 'active' ao link clicado
        link.classList.add('active');
        
        // Mostra a seção correspondente
        const sectionId = link.id.replace('nav-', '') + '-section';
        const targetSection = document.getElementById(sectionId);
        if (targetSection) {
            targetSection.classList.add('active-section');
        } else {
            console.warn(`Seção com ID '${sectionId}' não encontrada.`);
        }
    }

    /**
     * Lida com a busca de produtos.
     */
    async #handleSearch() {
        const searchTerm = this.elements.searchInput.value.trim();
        await this.#loadProducts(searchTerm);
    }

    /**
     * Carrega e exibe a lista de produtos.
     * @param {string} [searchTerm=''] - O termo de busca (opcional).
     */
    async #loadProducts(searchTerm = '') {
        try {
            this.elements.productsContainer.innerHTML = '<div class="loading">Carregando produtos...</div>';
            
            const products = searchTerm 
                ? await productService.searchProductsByName(searchTerm)
                : await productService.getAllProducts();
            
            this.#displayProducts(products);
        } catch (error) {
            console.error('Erro ao carregar produtos:', error);
            this.elements.productsContainer.innerHTML = `<div class="error">Erro ao carregar produtos: ${error.message}</div>`;
        }
    }

    /**
     * Exibe os produtos na interface.
     * @param {Array} products - A lista de produtos a serem exibidos.
     */
    #displayProducts(products) {
        if (products.length === 0) {
            this.elements.productsContainer.innerHTML = '<div class="no-results">Nenhum produto encontrado</div>';
            return;
        }

        this.elements.productsContainer.innerHTML = ''; // Limpa o container
        
        products.forEach(product => {
            const productCard = document.createElement('div');
            productCard.className = 'product-card';
            
            // Define a imagem principal ou um placeholder
            const mainImage = product.imagens && product.imagens.length > 0 
                ? `http://localhost:8080/api/produtos/${product.id}/imagem/${product.imagens[0]}`
                : 'images/placeholder.png'; // Caminho para uma imagem de placeholder local
            
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
                        <span class="price">R$ ${product.preco.toFixed(2)}</span>
                        <span class="quantity">${product.quantidade} em estoque</span>
                    </div>
                </div>
                <div class="product-actions">
                    <button class="btn-edit" data-id="${product.id}">
                        <i class="fas fa-edit"></i> Editar
                    </button>
                    <button class="btn-delete" data-id="${product.id}">
                        <i class="fas fa-trash"></i> Excluir
                    </button>
                </div>
            `;
            
            this.elements.productsContainer.appendChild(productCard);
            
            productCard.querySelector('.btn-edit').addEventListener('click', () => this.#openEditModal(product.id));
            productCard.querySelector('.btn-delete').addEventListener('click', () => this.#confirmDelete(product.id));
        });
    }

    /**
     * Abre o modal para adicionar ou editar um produto.
     * @param {Object|null} [product=null] - O objeto produto a ser editado, ou null para um novo produto.
     */
    #openProductModal(product = null) {
        this.#resetProductForm();
        this.newImagesToUpload = []; // Limpa a lista de novas imagens ao abrir o modal
        
        if (product) {
            this.#fillProductForm(product);
            this.elements.modalTitle.textContent = 'Editar Produto';
        } else {
            this.elements.modalTitle.textContent = 'Adicionar Novo Produto';
        }
        
        this.elements.productModal.style.display = 'block';
    }

    /**
     * Abre o modal de edição, buscando os dados do produto.
     * @param {string} productId - O ID do produto a ser editado.
     */
    async #openEditModal(productId) {
        try {
            const product = await productService.getProductById(productId);
            this.#openProductModal(product);
        } catch (error) {
            console.error('Erro ao abrir modal de edição:', error);
            alert(`Erro ao carregar produto para edição: ${error.message}`);
        }
    }

    /**
     * Fecha o modal de produto.
     */
    #closeModal() {
        this.elements.productModal.style.display = 'none';
        this.newImagesToUpload = []; // Garante que as imagens pendentes sejam limpas
    }

    /**
     * Reseta o formulário do produto para os valores padrão.
     */
    #resetProductForm() {
        this.elements.productForm.reset();
        this.elements.productImagesContainer.innerHTML = ''; // Limpa o container de imagens
        this.elements.productIdInput.value = '';
    }

    /**
     * Preenche o formulário do produto com os dados de um produto existente.
     * @param {Object} product - O objeto produto.
     */
    #fillProductForm(product) {
        this.elements.productIdInput.value = product.id || '';
        this.elements.productNameInput.value = product.nome || '';
        this.elements.productDescriptionInput.value = product.textoDescritivo || '';
        this.elements.productManufacturerInput.value = product.fabricante || '';
        this.elements.productColorInput.value = product.cor || '';
        this.elements.productPriceInput.value = product.preco || '';
        this.elements.productQuantityInput.value = product.quantidade || '';
        
        this.elements.productImagesContainer.innerHTML = '';
        
        // Exibe imagens existentes
        if (product.imagens && product.imagens.length > 0) {
            product.imagens.forEach((imageName, index) => {
                const imageDiv = document.createElement('div');
                imageDiv.className = 'existing-image';
                imageDiv.innerHTML = `
                    <img src="http://localhost:8080/api/produtos/${product.id}/imagem/${imageName}" 
                         alt="Imagem ${index + 1}" 
                         class="preview-image">
                    <button type="button" class="btn-remove-image" data-image-name="${imageName}">
                        <i class="fas fa-times"></i>
                    </button>
                `;
                this.elements.productImagesContainer.appendChild(imageDiv);
                
                // Lógica para remover imagem existente (apenas do DOM, backend precisa de endpoint)
                imageDiv.querySelector('.btn-remove-image').addEventListener('click', () => {
                    // TODO: Se desejar remover a imagem do backend, você precisaria fazer uma chamada DELETE aqui.
                    // Por agora, apenas remove do DOM.
                    imageDiv.remove();
                    alert('Funcionalidade de exclusão de imagem existente no servidor não implementada. A imagem foi removida apenas visualmente.');
                });
            });
        }
    }

    /**
     * Adiciona um campo de seleção de imagem (input type="file").
     */
    #addImageField() {
        const fileInput = document.createElement('input');
        fileInput.type = 'file';
        fileInput.accept = 'image/*';
        fileInput.multiple = true; // Permite seleção múltipla
        fileInput.style.display = 'none'; // Esconde o input real

        fileInput.addEventListener('change', (e) => {
            if (e.target.files.length > 0) {
                Array.from(e.target.files).forEach(file => {
                    this.newImagesToUpload.push(file); // Adiciona o arquivo File ao array de upload
                    const reader = new FileReader();
                    
                    reader.onload = (event) => {
                        const imageDiv = document.createElement('div');
                        imageDiv.className = 'image-upload';
                        imageDiv.innerHTML = `
                            <img src="${event.target.result}" class="preview-image">
                            <button type="button" class="btn-remove-image">
                                <i class="fas fa-times"></i>
                            </button>
                        `;
                        
                        this.elements.productImagesContainer.appendChild(imageDiv);
                        
                        // Evento para remover a imagem recém-adicionada (do DOM e da lista de upload)
                        imageDiv.querySelector('.btn-remove-image').addEventListener('click', () => {
                            imageDiv.remove();
                            this.newImagesToUpload = this.newImagesToUpload.filter(imgFile => imgFile !== file);
                        });
                    };
                    reader.readAsDataURL(file); // Lê o arquivo como URL para pré-visualização
                });
            }
        });
        
        fileInput.click(); // Simula o clique no input de arquivo
    }

    /**
     * Lida com o envio do formulário de produto (criação ou atualização).
     * @param {Event} e - O evento de submit.
     */
    async #handleFormSubmit(e) {
        e.preventDefault();
        
        const productId = this.elements.productIdInput.value;
        const productData = {
            nome: this.elements.productNameInput.value,
            textoDescritivo: this.elements.productDescriptionInput.value,
            fabricante: this.elements.productManufacturerInput.value,
            cor: this.elements.productColorInput.value,
            preco: parseFloat(this.elements.productPriceInput.value),
            quantidade: parseInt(this.elements.productQuantityInput.value),
            imagens: this.newImagesToUpload, // Usado para criação
            novasImagens: this.newImagesToUpload // Usado para atualização
        };

        try {
            if (productId) {
                await productService.updateProduct(productId, productData);
                alert('Produto atualizado com sucesso!');
            } else {
                await productService.createProduct(productData);
                alert('Produto criado com sucesso!');
            }
            
            await this.#loadProducts(); // Recarrega a lista
            this.#closeModal(); // Fecha o modal
        } catch (error) {
            console.error('Erro ao salvar produto:', error);
            alert(`Erro ao salvar produto: ${error.message}`);
        }
    }

    /**
     * Abre o modal de confirmação para exclusão.
     * @param {string} productId - O ID do produto a ser excluído.
     */
    #confirmDelete(productId) {
        this.currentProductId = productId;
        this.elements.confirmModal.style.display = 'block';
    }

    /**
     * Fecha o modal de confirmação.
     */
    #closeConfirmModal() {
        this.elements.confirmModal.style.display = 'none';
        this.currentProductId = null;
    }

    /**
     * Lida com a ação de confirmação (exclusão).
     */
    async #handleConfirmAction() {
        if (!this.currentProductId) return;
        
        try {
            await productService.deleteProduct(this.currentProductId);
            alert('Produto excluído com sucesso!');
            await this.#loadProducts();
            this.#closeConfirmModal();
        } catch (error) {
            console.error('Erro ao excluir produto:', error);
            alert(`Erro ao excluir produto: ${error.message}`);
            this.#closeConfirmModal();
        }
    }

    /**
     * Carrega e renderiza os gráficos de dados (simulados).
     */
    #loadCharts() {
        // Verifica se os contextos dos gráficos existem antes de tentar criar gráficos
        if (!this.elements.manufacturerChartCtx || 
            !this.elements.colorChartCtx || 
            !this.elements.stockChartCtx) {
            console.warn("Um ou mais elementos de canvas para gráficos não foram encontrados.");
            return;
        }

        // Dados de simulação (em uma aplicação real, você buscaria isso da API)
        const products = [
            { fabricante: 'JBL', cor: 'Preto', quantidade: 19 },
            { fabricante: 'Philips', cor: 'Branco', quantidade: 27 },
            { fabricante: 'Acer', cor: 'Preto', quantidade: 18 },
            { fabricante: 'Fortrek', cor: 'Preto', quantidade: 12 },
            { fabricante: 'JBL', cor: 'Preto', quantidade: 3 },
            { fabricante: 'Fortrek', cor: 'Preto', quantidade: 11 },
            { fabricante: 'Hyperx', cor: 'Preto', quantidade: 5 },
            { fabricante: 'JBL', cor: 'Preto', quantidade: 14 },
            { fabricante: 'Fortrek', cor: 'Branco', quantidade: 8 },
            { fabricante: 'Redragon', cor: 'Vermelho', quantidade: 6 },
            { fabricante: 'JBL', cor: 'Branco', quantidade: 12 },
            { fabricante: 'Redragon', cor: 'Azul', quantidade: 8 }
        ];
        
        const manufacturerData = this.#processManufacturerData(products);
        const colorData = this.#processColorData(products);
        const topProducts = this.#processTopProducts(products);
        
        this.#createManufacturerChart(manufacturerData);
        this.#createColorChart(colorData);
        this.#createStockChart(topProducts);
    }

    /**
     * Processa dados de produtos para o gráfico de fabricantes.
     * @param {Array} products - Lista de produtos.
     * @returns {Object} Dados para o gráfico (labels e data).
     */
    #processManufacturerData(products) {
        const manufacturers = {};
        products.forEach(product => {
            manufacturers[product.fabricante] = (manufacturers[product.fabricante] || 0) + product.quantidade;
        });
        return {
            labels: Object.keys(manufacturers),
            data: Object.values(manufacturers)
        };
    }

    /**
     * Processa dados de produtos para o gráfico de cores.
     * @param {Array} products - Lista de produtos.
     * @returns {Object} Dados para o gráfico (labels e data).
     */
    #processColorData(products) {
        const colors = {};
        products.forEach(product => {
            colors[product.cor] = (colors[product.cor] || 0) + product.quantidade;
        });
        return {
            labels: Object.keys(colors),
            data: Object.values(colors)
        };
    }

    /**
     * Processa os produtos para encontrar os com maior estoque.
     * @param {Array} products - Lista de produtos.
     * @returns {Array} Top 5 produtos por quantidade.
     */
    #processTopProducts(products) {
        const productMap = {};
        products.forEach(product => {
            const key = `${product.fabricante} ${product.cor}`;
            productMap[key] = {
                nome: key,
                quantidade: (productMap[key]?.quantidade || 0) + product.quantidade,
                fabricante: product.fabricante
            };
        });
        return Object.values(productMap)
            .sort((a, b) => b.quantidade - a.quantidade)
            .slice(0, 5);
    }

    /**
     * Cria o gráfico de produtos por fabricante.
     * @param {Object} data - Dados para o gráfico.
     */
    #createManufacturerChart(data) {
        new Chart(this.elements.manufacturerChartCtx, {
            type: 'bar',
            data: {
                labels: data.labels,
                datasets: [{
                    label: 'Produtos por Fabricante',
                    data: data.data,
                    backgroundColor: 'rgba(54, 162, 235, 0.7)',
                    borderColor: 'rgba(54, 162, 235, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                plugins: { legend: { display: false } },
                scales: {
                    y: {
                        beginAtZero: true,
                        title: { display: true, text: 'Quantidade em Estoque' }
                    }
                }
            }
        });
    }

    /**
     * Cria o gráfico de distribuição por cor.
     * @param {Object} data - Dados para o gráfico.
     */
    #createColorChart(data) {
        new Chart(this.elements.colorChartCtx, {
            type: 'doughnut',
            data: {
                labels: data.labels,
                datasets: [{
                    label: 'Distribuição por Cor',
                    data: data.data,
                    backgroundColor: [
                        'rgba(0, 0, 0, 0.7)',      // Preto
                        'rgba(255, 255, 255, 0.7)', // Branco
                        'rgba(255, 0, 0, 0.7)',     // Vermelho
                        'rgba(0, 0, 255, 0.7)',     // Azul
                        'rgba(0, 128, 0, 0.7)'      // Verde
                    ],
                    borderColor: [
                        'rgba(0, 0, 0, 1)',
                        'rgba(200, 200, 200, 1)',
                        'rgba(255, 0, 0, 1)',
                        'rgba(0, 0, 255, 1)',
                        'rgba(0, 128, 0, 1)'
                    ],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                plugins: { legend: { position: 'bottom' } }
            }
        });
    }

    /**
     * Cria o gráfico dos produtos com maior estoque.
     * @param {Array} topProducts - Lista dos top produtos.
     */
    #createStockChart(topProducts) {
        new Chart(this.elements.stockChartCtx, {
            type: 'horizontalBar', // Chart.js v2 usa 'horizontalBar'
            data: {
                labels: topProducts.map(p => p.nome),
                datasets: [{
                    label: 'Quantidade em Estoque',
                    data: topProducts.map(p => p.quantidade),
                    backgroundColor: 'rgba(54, 162, 235, 0.7)',
                    borderColor: 'rgba(54, 162, 235, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            afterLabel: (context) => {
                                const product = topProducts[context.dataIndex];
                                return `Fabricante: ${product.fabricante}`;
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        beginAtZero: true,
                        title: { display: true, text: 'Quantidade em Estoque' }
                    }
                }
            }
        });
    }
}

// Inicialização da aplicação quando o DOM estiver pronto
document.addEventListener('DOMContentLoaded', () => {
    new UIController();
});