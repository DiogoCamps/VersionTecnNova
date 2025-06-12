document.addEventListener('DOMContentLoaded', async () => {
    const API_BASE_URL = 'http://localhost:8080/api/produtos';

    const manufacturerChartCtx = document.getElementById('manufacturer-chart')?.getContext('2d');
    const colorChartCtx = document.getElementById('color-chart')?.getContext('2d');
    const stockChartCtx = document.getElementById('stock-chart')?.getContext('2d');

    // Se os elementos do canvas não existirem, não faz nada
    if (!manufacturerChartCtx || !colorChartCtx || !stockChartCtx) {
        console.error('Elementos de canvas para os gráficos não foram encontrados.');
        return;
    }

    /**
     * Busca todos os produtos da API.
     * @returns {Promise<Array>}
     */
    const fetchAllProducts = async () => {
        try {
            const response = await fetch(API_BASE_URL);
            if (!response.ok) {
                throw new Error(`Erro ao buscar dados: ${response.statusText}`);
            }
            return await response.json();
        } catch (error) {
            console.error('Falha ao carregar dados para os relatórios:', error);
            // Poderia exibir uma mensagem de erro na tela aqui
            return []; // Retorna array vazio em caso de erro
        }
    };

    /**
     * Processa os dados para o gráfico de fabricantes.
     * @param {Array<object>} products - Lista de produtos.
     * @returns {{labels: string[], data: number[]}}
     */
    const processManufacturerData = (products) => {
        const data = products.reduce((acc, product) => {
            acc[product.fabricante] = (acc[product.fabricante] || 0) + 1;
            return acc;
        }, {});
        return {
            labels: Object.keys(data),
            data: Object.values(data)
        };
    };

    /**
     * Processa os dados para o gráfico de cores.
     * @param {Array<object>} products - Lista de produtos.
     * @returns {{labels: string[], data: number[]}}
     */
    const processColorData = (products) => {
        const data = products.reduce((acc, product) => {
            acc[product.cor] = (acc[product.cor] || 0) + 1;
            return acc;
        }, {});
        return {
            labels: Object.keys(data),
            data: Object.values(data)
        };
    };

    /**
     * Processa os dados para o gráfico de top 5 em estoque.
     * @param {Array<object>} products - Lista de produtos.
     * @returns {{labels: string[], data: number[]}}
     */
    const processTopStockData = (products) => {
        const sortedProducts = [...products].sort((a, b) => b.quantidade - a.quantidade).slice(0, 5);
        return {
            labels: sortedProducts.map(p => p.nome),
            data: sortedProducts.map(p => p.quantidade)
        };
    };

    /**
     * Cria os gráficos com os dados processados.
     * @param {Array<object>} products - Lista de produtos.
     */
    const createCharts = (products) => {
        const manufacturerData = processManufacturerData(products);
        const colorData = processColorData(products);
        const topStockData = processTopStockData(products);

        new Chart(manufacturerChartCtx, {
            type: 'bar',
            data: {
                labels: manufacturerData.labels,
                datasets: [{
                    label: 'Nº de Produtos',
                    data: manufacturerData.data,
                    backgroundColor: 'rgba(54, 162, 235, 0.7)',
                }]
            }
        });

        new Chart(colorChartCtx, {
            type: 'doughnut',
            data: {
                labels: colorData.labels,
                datasets: [{
                    label: 'Distribuição',
                    data: colorData.data,
                    backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40'],
                }]
            }
        });

        new Chart(stockChartCtx, {
            type: 'bar',
            data: {
                labels: topStockData.labels,
                datasets: [{
                    label: 'Quantidade em Estoque',
                    data: topStockData.data,
                    backgroundColor: 'rgba(75, 192, 192, 0.7)',
                }]
            },
            options: {
                indexAxis: 'y', // Transforma em gráfico de barras horizontais
            }
        });
    };

    // Inicia o processo
    const products = await fetchAllProducts();
    if (products.length > 0) {
        createCharts(products);
    }
});
