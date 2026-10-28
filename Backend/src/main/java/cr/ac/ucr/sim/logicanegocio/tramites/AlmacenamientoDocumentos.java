package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Guarda en el sistema de archivos los documentos de respaldo de un expediente.
 *   La HU-031 admite documentación atípica, por lo que no se restringe la extensión del archivo;
 *   sí se limita el tamaño mediante la configuración de carga de Spring.
 */
@Component
public class AlmacenamientoDocumentos {

    private final String vgc_RutaBase;

    public AlmacenamientoDocumentos(
            @Value("${app.ruta-documentos:./documentos}") String pvc_RutaBase) {
        this.vgc_RutaBase = pvc_RutaBase;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Guarda un archivo dentro de la carpeta del expediente y devuelve la ruta
     *   relativa que queda registrada en SIMF_DOCUMENTO.
     *
     * @param pvo_Archivo archivo recibido en la petición
     * @param pvc_ReferenciaInterna referencia interna del expediente, usada como carpeta
     * @return ruta relativa del archivo almacenado
     */
    public String GuardarArchivo(MultipartFile pvo_Archivo, String pvc_ReferenciaInterna) {
        String vlc_NombreOriginal = this.DepurarNombreArchivo(pvo_Archivo.getOriginalFilename());
        String vlc_NombreAlmacenado = UUID.randomUUID().toString().replace("-", "") + "_" + vlc_NombreOriginal;
        Path vlo_Carpeta = Paths.get(this.vgc_RutaBase, this.DepurarNombreCarpeta(pvc_ReferenciaInterna));

        try {
            Files.createDirectories(vlo_Carpeta);
            Path vlo_Destino = vlo_Carpeta.resolve(vlc_NombreAlmacenado);
            try (InputStream vlo_Contenido = pvo_Archivo.getInputStream()) {
                Files.copy(vlo_Contenido, vlo_Destino, StandardCopyOption.REPLACE_EXISTING);
            }
            return Paths.get(this.DepurarNombreCarpeta(pvc_ReferenciaInterna), vlc_NombreAlmacenado)
                    .toString()
                    .replace('\\', '/');
        } catch (IOException vlo_Excepcion) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DOCUMENTO_NO_ALMACENADO);
        }
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Elimina un archivo ya almacenado. Se utiliza para deshacer las escrituras
     *   cuando la transacción del registro no llega a confirmarse.
     *
     * @param pvc_RutaRelativa ruta relativa registrada en SIMF_DOCUMENTO
     */
    public void EliminarArchivo(String pvc_RutaRelativa) {
        try {
            Files.deleteIfExists(this.ObtenerRutaAbsoluta(pvc_RutaRelativa));
        } catch (IOException vlo_Excepcion) {
            // El archivo queda en disco sin registro asociado; no se interrumpe el flujo.
        }
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Resuelve la ruta absoluta de un archivo almacenado y comprueba que quede
     *   dentro de la carpeta de documentos, de manera que una ruta manipulada no permita leer
     *   otros archivos del servidor.
     *
     * @param pvc_RutaRelativa ruta relativa registrada en SIMF_DOCUMENTO
     * @return ruta absoluta del archivo
     */
    public Path ObtenerRutaAbsoluta(String pvc_RutaRelativa) {
        Path vlo_Base = Paths.get(this.vgc_RutaBase).toAbsolutePath().normalize();
        Path vlo_Destino = vlo_Base.resolve(pvc_RutaRelativa).normalize();
        if (!vlo_Destino.startsWith(vlo_Base)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DOCUMENTO_NO_DISPONIBLE);
        }
        return vlo_Destino;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Lee el contenido de un archivo almacenado para entregarlo en la descarga.
     *
     * @param pvc_RutaRelativa ruta relativa registrada en SIMF_DOCUMENTO
     * @return contenido del archivo
     */
    public byte[] LeerArchivo(String pvc_RutaRelativa) {
        try {
            return Files.readAllBytes(this.ObtenerRutaAbsoluta(pvc_RutaRelativa));
        } catch (IOException vlo_Excepcion) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DOCUMENTO_NO_DISPONIBLE);
        }
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Extrae el nombre del archivo descartando cualquier ruta enviada por el cliente,
     *   de manera que no sea posible escribir fuera de la carpeta de documentos.
     *
     * @param pvc_NombreArchivo nombre recibido en la petición
     * @return nombre de archivo seguro
     */
    public String DepurarNombreArchivo(String pvc_NombreArchivo) {
        if (pvc_NombreArchivo == null || pvc_NombreArchivo.isBlank()) {
            return "documento";
        }
        String vlc_Nombre = Paths.get(pvc_NombreArchivo.replace('\\', '/')).getFileName().toString();
        vlc_Nombre = vlc_Nombre.replaceAll("[^A-Za-z0-9._-]", "_");
        int vln_LongitudMaxima = 100; // 100 = margen bajo el VARCHAR(120) de NOMBRE_ARCHIVO
        if (vlc_Nombre.length() > vln_LongitudMaxima) {
            vlc_Nombre = vlc_Nombre.substring(vlc_Nombre.length() - vln_LongitudMaxima);
        }
        return vlc_Nombre;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Convierte la referencia interna en un nombre de carpeta seguro.
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @return nombre de carpeta sin separadores de ruta
     */
    private String DepurarNombreCarpeta(String pvc_ReferenciaInterna) {
        if (pvc_ReferenciaInterna == null || pvc_ReferenciaInterna.isBlank()) {
            return "sin-referencia";
        }
        return pvc_ReferenciaInterna.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9._-]", "_");
    }
}
