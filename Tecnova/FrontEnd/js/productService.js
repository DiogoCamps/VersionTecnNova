class ProductService {
    constructor() {
        this.baseUrl = 'http://localhost:8080/api/produtos';
    }

    // Listar todos os produtos
    async getAllProducts() {
        try {
            const response = await fetch(this.baseUrl);
            if (!response.ok) {
                throw new Error('Erro ao carregar produtos');
            }
            return await response.json();
        } catch (error) {
            console.error('Erro no ProductService.getAllProducts:', error);
            throw error;
        }
    }

    // Buscar produto por ID
    async getProductById(id) {
        try {
            const response = await fetch(`${this.baseUrl}/${id}`);
            if (!response.ok) {
                throw new Error('Produto não encontrado');
            }
            return await response.json();
        } catch (error) {
            console.error('Erro no ProductService.getProductById:', error);
            throw error;
        }
    }

    // Buscar produtos por nome
    async searchProductsByName(name) {
        try {
            const response = await fetch(`${this.baseUrl}?nome=${encodeURIComponent(name)}`);
            if (!response.ok) {
                throw new Error('Erro na busca de produtos');
            }
            return await response.json();
        } catch (error) {
            console.error('Erro no ProductService.searchProductsByName:', error);
            throw error;
        }
    }

    // Criar novo produto
    async createProduct(productData) {
        try {
            const formData = new FormData();
            
            // Adiciona campos do produto ao FormData
            const product = {
                nome: productData.nome,
                textoDescritivo: productData.textoDescritivo,
                cor: productData.cor,
                fabricante: productData.fabricante,
                preco: productData.preco,
                quantidade: productData.quantidade
            };
            
            formData.append('produto', new Blob([JSON.stringify(product)], { type: 'application/json' }));
            
            // Adiciona imagens ao FormData
            if (productData.imagens && productData.imagens.length > 0) {
                for (let i = 0; i < productData.imagens.length; i++) {
                    if (productData.imagens[i].file) {
                        formData.append('imagem', productData.imagens[i].file);
                    }
                }
            }

            const response = await fetch(this.baseUrl, {
                method: 'POST',
                body: formData
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Erro ao criar produto');
            }

            return await response.json();
        } catch (error) {
            console.error('Erro no ProductService.createProduct:', error);
            throw error;
        }
    }

    // Atualizar produto existente
    async updateProduct(id, productData) {
        try {
            const formData = new FormData();
            
            // Adiciona campos do produto ao FormData
            const product = {
                nome: productData.nome,
                textoDescritivo: productData.textoDescritivo,
                cor: productData.cor,
                fabricante: productData.fabricante,
                preco: productData.preco,
                quantidade: productData.quantidade
            };
            
            formData.append('produto', new Blob([JSON.stringify(product)], { type: 'application/json' }));
            
            // Adiciona novas imagens ao FormData
            if (productData.novasImagens && productData.novasImagens.length > 0) {
                for (let i = 0; i < productData.novasImagens.length; i++) {
                    if (productData.novasImagens[i].file) {
                        formData.append('imagem', productData.novasImagens[i].file);
                    }
                }
            }

            const response = await fetch(`${this.baseUrl}/${id}`, {
                method: 'PUT',
                body: formData
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Erro ao atualizar produto');
            }

            return await response.json();
        } catch (error) {
            console.error('Erro no ProductService.updateProduct:', error);
            throw error;
        }
    }

    // Excluir produto
    async deleteProduct(id) {
        try {
            const response = await fetch(`${this.baseUrl}/${id}`, {
                method: 'DELETE'
            });

            if (!response.ok) {
                throw new Error('Erro ao excluir produto');
            }

            return true;
        } catch (error) {
            console.error('Erro no ProductService.deleteProduct:', error);
            throw error;
        }
    }
}

const productService = new ProductService();