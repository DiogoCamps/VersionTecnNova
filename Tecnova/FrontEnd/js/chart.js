document.addEventListener('DOMContentLoaded', function() {
    const manufacturerChartCtx = document.getElementById('manufacturer-chart').getContext('2d');
    const colorChartCtx = document.getElementById('color-chart').getContext('2d');
    const stockChartCtx = document.getElementById('stock-chart').getContext('2d');
    
    // Simulação de dados - na implementação real, você buscaria esses dados da API
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
    
    // Processamento dos dados para os gráficos
    const manufacturerData = processManufacturerData(products);
    const colorData = processColorData(products);
    const topProducts = processTopProducts(products);
    
    // Criação dos gráficos
    createManufacturerChart(manufacturerChartCtx, manufacturerData);
    createColorChart(colorChartCtx, colorData);
    createStockChart(stockChartCtx, topProducts);
    
    function processManufacturerData(products) {
        const manufacturers = {};
        
        products.forEach(product => {
            if (!manufacturers[product.fabricante]) {
                manufacturers[product.fabricante] = 0;
            }
            manufacturers[product.fabricante] += product.quantidade;
        });
        
        return {
            labels: Object.keys(manufacturers),
            data: Object.values(manufacturers)
        };
    }
    
    function processColorData(products) {
        const colors = {};
        
        products.forEach(product => {
            if (!colors[product.cor]) {
                colors[product.cor] = 0;
            }
            colors[product.cor] += product.quantidade;
        });
        
        return {
            labels: Object.keys(colors),
            data: Object.values(colors)
        };
    }
    
    function processTopProducts(products) {
        // Agrupa produtos por nome e soma as quantidades
        const productMap = {};
        
        products.forEach(product => {
            if (!productMap[product.nome]) {
                productMap[product.nome] = {
                    nome: product.nome,
                    quantidade: 0,
                    fabricante: product.fabricante
                };
            }
            productMap[product.nome].quantidade += product.quantidade;
        });
        
        // Converte para array e ordena por quantidade
        const productArray = Object.values(productMap);
        productArray.sort((a, b) => b.quantidade - a.quantidade);
        
        // Pega os top 5
        return productArray.slice(0, 5);
    }
    
    function createManufacturerChart(ctx, data) {
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: data.labels,
                datasets: [{
                    label: 'Produtos por Fabricante',
                    data: data.data,
                    backgroundColor: [
                        'rgba(255, 99, 132, 0.7)',
                        'rgba(54, 162, 235, 0.7)',
                        'rgba(255, 206, 86, 0.7)',
                        'rgba(75, 192, 192, 0.7)',
                        'rgba(153, 102, 255, 0.7)',
                        'rgba(255, 159, 64, 0.7)'
                    ],
                    borderColor: [
                        'rgba(255, 99, 132, 1)',
                        'rgba(54, 162, 235, 1)',
                        'rgba(255, 206, 86, 1)',
                        'rgba(75, 192, 192, 1)',
                        'rgba(153, 102, 255, 1)',
                        'rgba(255, 159, 64, 1)'
                    ],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: 'Quantidade em Estoque'
                        }
                    }
                }
            }
        });
    }
    
    function createColorChart(ctx, data) {
        new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: data.labels,
                datasets: [{
                    label: 'Distribuição por Cor',
                    data: data.data,
                    backgroundColor: [
                        'rgba(0, 0, 0, 0.7)',       // Preto
                        'rgba(255, 255, 255, 0.7)',   // Branco
                        'rgba(255, 0, 0, 0.7)',       // Vermelho
                        'rgba(0, 0, 255, 0.7)',       // Azul
                        'rgba(0, 128, 0, 0.7)'        // Verde (caso tenha)
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
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
    }
    
    function createStockChart(ctx, topProducts) {
        new Chart(ctx, {
            type: 'horizontalBar',
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
                    legend: {
                        display: false
                    },
                    tooltip: {
                        callbacks: {
                            afterLabel: function(context) {
                                const product = topProducts[context.dataIndex];
                                return `Fabricante: ${product.fabricante}`;
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: 'Quantidade em Estoque'
                        }
                    }
                }
            }
        });
    }
});