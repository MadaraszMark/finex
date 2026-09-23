package hu.finex.main.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hu.finex.main.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Kategória keresése pontos név alapján
    Optional<Category> findByNameIgnoreCase(String name);

    // Név ütközés ellenőrzés (új kategória létrehozásakor fontos)
    boolean existsByNameIgnoreCase(String name);

    // Összes kategória név szerint rendezve
    List<Category> findAllByOrderByNameAsc();
}
