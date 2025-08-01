package Wallapick.Servicios;

import Wallapick.Modelos.Compra;
import Wallapick.Modelos.Producto;
import Wallapick.Modelos.Usuario;
import Wallapick.Repositorios.CompraRepositorio;
import Wallapick.Repositorios.ProductoRepositorio;
import Wallapick.Repositorios.UsuarioRepositorio;
import Wallapick.Utils.JWTUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ProductoServicio {
    @Autowired
    private  ProductoRepositorio productoRepositorio;
    @Autowired
    private  UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private CompraRepositorio compraRepositorio;
    @Autowired
    private JWTUser jwtUser;



    public List<Producto> buscarProductosPorNombreParcial(String nombreParcial) {
        return productoRepositorio.findByNombreContainingIgnoreCase(nombreParcial);
    }

    public List<Producto> obtenerProductosDeUsuarioLogueado(String token) {
        try {
            Usuario usuario = jwtUser.ObtenerUsuario(token);

            if ("LOGGED".equals(usuario.getRole())) {
                return productoRepositorio.findByVendedor_Id(usuario.getId());
            } else {
                //No logeado
                return new ArrayList<>();
            }
        } catch (Exception e) {
            // Token inválido u otro error
            return new ArrayList<>();


        }
    }

    public int createProduct(Producto producto, String token) {
        try {
            Usuario usuario = jwtUser.ObtenerUsuario(token);

            if (usuario.getRole().equalsIgnoreCase("LOGGED")) {
                Usuario vendedor = usuarioRepositorio.findById(usuario.getId())
                        .orElse(null);
                if (vendedor == null) {
                    return 0;
                }

                producto.setVendedor(vendedor);
                producto.setPrecio(producto.getPrecio() * 0.26);
                producto.setFechaPublicacion(new Date());
                productoRepositorio.save(producto);

                return 1; // éxito
            }

            return 0; // usuario no tiene rol adecuado
        } catch (Exception e) {
            return -1; // error interno
        }
    }
    public List<Producto> getAllProducts() {
        return productoRepositorio.findAll();
    }

    public int deleteProduct(long id, String token) {
        try {
            Usuario usuario = jwtUser.ObtenerUsuario(token);
            Producto producto = productoRepositorio.findById(id).orElse(null);

            if (producto != null && producto.getVendedor().getId() == usuario.getId()) {
                productoRepositorio.delete(producto);
                return 1; // Producto eliminado correctamente
            }
            return 0; // No se pudo eliminar el producto
        } catch (Exception e) {
            return -1; // Error al intentar eliminar el producto
        }
    }

    public int updateProduct(Producto producto, String token) {
        try {
            Usuario usuario = jwtUser.ObtenerUsuario(token);
            Producto existingProduct = productoRepositorio.findById(producto.getId()).orElse(null);

            if (existingProduct != null && existingProduct.getVendedor().getId() == usuario.getId()) {
                existingProduct.setNombre(producto.getNombre());
                existingProduct.setDescripcion(producto.getDescripcion());
                existingProduct.setPrecio(producto.getPrecio());
                existingProduct.setFechaPublicacion(new Date());
                productoRepositorio.save(existingProduct);
                return 1; // Producto actualizado correctamente
            }
            return 0; // No se pudo actualizar el producto
        } catch (Exception e) {
            return -1; // Error al intentar actualizar el producto
        }
    }

    public int comprarProductos(ArrayList<Long> ids, String token) {
        try {
            Usuario usuario = jwtUser.ObtenerUsuario(token);

            if (!"LOGGED".equals(usuario.getRole())) {
                return 0; // Usuario no autorizado
            }

            for (Long id : ids) {
                Producto producto = productoRepositorio.findById(id).orElse(null);

                if (producto == null || !producto.isEnVenta()) {
                    return -1; // Producto no encontrado o no está en venta
                }

                Usuario vendedor = usuarioRepositorio.findById(producto.getVendedor().getId()).orElse(null);
                if (vendedor == null) {
                    return -1; // Vendedor no encontrado
                }

                Compra com = new Compra(producto, usuario, vendedor, new Date(), producto.getPrecio());
                compraRepositorio.save(com);

                producto.setEnVenta(false); // Marcar como vendido
                productoRepositorio.save(producto);
            }

            return 1; // Compra exitosa
        } catch (Exception e) {
            e.printStackTrace(); // Ayuda a depurar
            return 2; // Error al intentar comprar
        }
    }

}
