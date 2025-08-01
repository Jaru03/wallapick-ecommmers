package Wallapick.Repositorios;

import Wallapick.Modelos.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario,Long> {
    Usuario findByUsername(String username);

}
