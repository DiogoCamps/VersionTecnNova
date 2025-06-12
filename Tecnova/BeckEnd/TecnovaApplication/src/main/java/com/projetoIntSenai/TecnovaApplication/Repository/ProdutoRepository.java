package com.projetoIntSenai.TecnovaApplication.Repository;

import com.projetoIntSenai.TecnovaApplication.Entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Busca produtos cujo nome contenha a string, ignorando maiúsculas/minúsculas
    List<Produto> findByNomeContainingIgnoreCase(String nome);

    // Sobrescreve o método findById para garantir que a coleção de imagens seja carregada (EAGER fetch)
    // Isso evita o erro "LazyInitializationException" se as imagens forem acessadas fora de uma transação.
    @Query("SELECT p FROM Produto p LEFT JOIN FETCH p.imagens WHERE p.id = :id")
    @Override
    Optional<Produto> findById(Long id);
}
