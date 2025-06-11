document.addEventListener('DOMContentLoaded', function() {
    // Elementos da interface
    const homeSection = document.getElementById('home-section');
    const productsSection = document.getElementById('products-section');
    const productsContainer = document.getElementById('products-container');
    const searchInput = document.getElementById('search-input');
    const searchBtn = document.getElementById('search-btn');
    const addProductBtn = document.getElementById('add-product-btn');
    const productModal = document.getElementById('product-modal');
    const productForm = document.getElementById('product-form');
    const closeModalBtn = document.querySelector('.close-btn');
    const cancelBtn = document.getElementById('cancel-btn');
    
    // Navegação entre seções
    document.querySelectorAll('nav a').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Remove classe active de todos os links
            document.querySelectorAll('nav a').forEach(navLink => {
                navLink.classList.remove('active');
            });
            
            // Adiciona classe active ao link clicado
            this.classList.add('active');
            
            // Esconde todas as seções
            document.querySelectorAll('main section').forEach(section => {
                section.classList.remove('active-section');
            });
            
            // Mostra a seção correspondente
            const sectionId = this.id.replace('nav-', '') + '-section';
            document.getElementById(sectionId).classList.add('active-section');
        });
    });

    // Carrega os produtos ao abrir a página
    loadProducts();

    // Função para carregar produtos
    async function loadProducts(searchTerm = '') {
        try {
            productsContainer.innerHTML = '<div class="loading">Carregando produtos...</div>';
            
            let products;
            if (searchTerm) {
                products = await productService.searchProductsByName(searchTerm);
            } else {
                products = await productService.getAllProducts();
            }
            
            displayProducts(products);
        } catch (error) {
            console.error('Erro ao carregar produtos:', error);
            productsContainer.innerHTML = `<div class="error">Erro ao carregar produtos: ${error.message}</div>`;
        }
    }

    // Função para exibir produtos na grade
    function displayProducts(products) {
        if (products.length === 0) {
            productsContainer.innerHTML = '<div class="no-results">Nenhum produto encontrado</div>';
            return;
        }

        productsContainer.innerHTML = '';
        
        products.forEach(product => {
            const productCard = document.createElement('div');
            productCard.className = 'product-card';
            
            // Primeira imagem ou placeholder se não houver imagens
            const mainImage = product.imagens && product.imagens.length > 0 
                ? product.imagens[0] 
                : 'images/placeholder.png';
            
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
            
            productsContainer.appendChild(productCard);
        });

        // Adiciona eventos aos botões de editar e excluir
        document.querySelectorAll('.btn-edit').forEach(btn => {
            btn.addEventListener('click', () => openEditModal(btn.dataset.id));
        });
        
        document.querySelectorAll('.btn-delete').forEach(btn => {
            btn.addEventListener('click', () => confirmDelete(btn.dataset.id));
        });
    }

    // Função para abrir modal de edição
    async function openEditModal(productId) {
        try {
            const product = await productService.getProductById(productId);
            fillProductForm(product);
            productModal.style.display = 'block';
        } catch (error) {
            console.error('Erro ao abrir modal de edição:', error);
            alert(`Erro ao carregar produto: ${error.message}`);
        }
    }

    // Função para preencher formulário
    function fillProductForm(product) {
        document.getElementById('product-id').value = product.id || '';
        document.getElementById('product-name').value = product.nome || '';
        document.getElementById('product-description').value = product.textoDescritivo || '';
        document.getElementById('product-manufacturer').value = product.fabricante || '';
        document.getElementById('product-color').value = product.cor || '';
        document.getElementById('product-price').value = product.preco || '';
        document.getElementById('product-quantity').value = product.quantidade || '';
        
        // Limpa imagens anteriores
        const imagesContainer = document.getElementById('product-images-container');
        imagesContainer.innerHTML = '';
        
        // Adiciona imagens existentes
        if (product.imagens && product.imagens.length > 0) {
            product.imagens.forEach((image, index) => {
                const imageDiv = document.createElement('div');
                imageDiv.className = 'existing-image';
                imageDiv.innerHTML = `
                    <img src="${image}" alt="Imagem ${index + 1}" class="preview-image">
                    <button type="button" class="btn-remove-image" data-image-index="${index}">
                        <i class="fas fa-times"></i>
                    </button>
                `;
                imagesContainer.appendChild(imageDiv);
            });
        }
        
        // Atualiza título do modal
        document.getElementById('modal-title').textContent = product.id ? 'Editar Produto' : 'Adicionar Produto';
    }

    // Evento de busca
    searchBtn.addEventListener('click', () => {
        loadProducts(searchInput.value.trim());
    });

    searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            loadProducts(searchInput.value.trim());
        }
    });

    // Modal de adicionar produto
    addProductBtn.addEventListener('click', () => {
        // Limpa o formulário
        productForm.reset();
        document.getElementById('product-images-container').innerHTML = '';
        document.getElementById('modal-title').textContent = 'Adicionar Novo Produto';
        productModal.style.display = 'block';
    });

    // Fechar modal
    closeModalBtn.addEventListener('click', () => {
        productModal.style.display = 'none';
    });

    cancelBtn.addEventListener('click', () => {
        productModal.style.display = 'none';
    });

    // Adicionar campo de imagem
    document.getElementById('add-image-btn').addEventListener('click', () => {
        const fileInput = document.createElement('input');
        fileInput.type = 'file';
        fileInput.accept = 'image/*';
        fileInput.style.display = 'none';
        
        fileInput.addEventListener('change', (e) => {
            if (e.target.files.length > 0) {
                const file = e.target.files[0];
                const reader = new FileReader();
                
                reader.onload = (event) => {
                    const imageDiv = document.createElement('div');
                    imageDiv.className = 'image-upload';
                    imageDiv.innerHTML = `
                        <img src="${event.target.result}" class="preview-image">
                        <button type="button" class="btn-remove-image">
                            <i class="fas fa-times"></i>
                        </button>
                        <input type="hidden" name="imagens[]" value="${file.name}">
                    `;
                    
                    // Adiciona o objeto file ao elemento para acesso posterior
                    imageDiv.file = file;
                    
                    document.getElementById('product-images-container').appendChild(imageDiv);
                    
                    // Adiciona evento para remover imagem
                    imageDiv.querySelector('.btn-remove-image').addEventListener('click', () => {
                        imageDiv.remove();
                    });
                };
                
                reader.readAsDataURL(file);
            }
        });
        
        fileInput.click();
    });

    // Envio do formulário
    productForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const productId = document.getElementById('product-id').value;
        const formData = {
            nome: document.getElementById('product-name').value,
            textoDescritivo: document.getElementById('product-description').value,
            fabricante: document.getElementById('product-manufacturer').value,
            cor: document.getElementById('product-color').value,
            preco: parseFloat(document.getElementById('product-price').value),
            quantidade: parseInt(document.getElementById('product-quantity').value),
            imagens: []
        };
        
        // Coleta as imagens enviadas
        const imageUploads = document.querySelectorAll('.image-upload');
        imageUploads.forEach(upload => {
            if (upload.file) {
                formData.imagens.push({
                    file: upload.file,
                    name: upload.file.name
                });
            }
        });
        
        try {
            if (productId) {
                // Atualizar produto existente
                await productService.updateProduct(productId, formData);
            } else {
                // Criar novo produto
                await productService.createProduct(formData);
            }
            
            // Recarrega a lista de produtos
            loadProducts();
            // Fecha o modal
            productModal.style.display = 'none';
        } catch (error) {
            console.error('Erro ao salvar produto:', error);
            alert(`Erro ao salvar produto: ${error.message}`);
        }
    });

    // Função para confirmar exclusão
    function confirmDelete(productId) {
        const confirmModal = document.getElementById('confirm-modal');
        const confirmOk = document.getElementById('confirm-ok');
        const confirmCancel = document.getElementById('confirm-cancel');
        
        confirmModal.style.display = 'block';
        
        confirmCancel.addEventListener('click', () => {
            confirmModal.style.display = 'none';
        });
        
        confirmOk.addEventListener('click', async () => {
            try {
                await productService.deleteProduct(productId);
                loadProducts();
                confirmModal.style.display = 'none';
            } catch (error) {
                console.error('Erro ao excluir produto:', error);
                alert(`Erro ao excluir produto: ${error.message}`);
                confirmModal.style.display = 'none';
            }
        });
    }
});