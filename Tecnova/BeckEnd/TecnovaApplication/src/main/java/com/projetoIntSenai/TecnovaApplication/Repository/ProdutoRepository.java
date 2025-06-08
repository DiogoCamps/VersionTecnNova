package com.projetoIntSenai.TecnovaApplication.Repository;

import com.projetoIntSenai.TecnovaApplication.Entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Buscar produtos cujo nome contenha a string, ignorando maiúsculas/minúsculas
    List<Produto> findByNomeContainingIgnoreCase(String nome);
}
