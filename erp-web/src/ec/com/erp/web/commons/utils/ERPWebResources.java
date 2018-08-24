package ec.com.erp.web.commons.utils;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ERPWebResources implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    public static final String MENSAJE_EXITO_REGISTRO = "mensaje.exito.registro";
    public static final String MENSAJE_ERROR_REGISTRO = "mensaje.error.registro";

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("ec.com.erp.web.commons.resources.ERPWeb");

    /**
     * Permite la obtención del valor de la clave del archivo de propiedades general
     * 
     * @param key - Clave del archivo de propiedades que se desea obtener
     * @return - Valor de la clave ingresadaS
     * @throws MissingResourceException
     */
    public static String getString(String key) throws MissingResourceException {
        return RESOURCE_BUNDLE.getString(key);
    }

    /**
     * Permite la obtención del valor de la clave del archivo de propiedades general con la asignación de los parámetros
     * 
     * @param key - Clave del archivo de propiedades que se desea obtener
     * @param parameters - Parámetros del mensaje
     * @return - Valor de la clave ingresada
     * @throws MissingResourceException
     */
    public static String getString(String key, Object[] parameters) throws MissingResourceException {
        return MessageFormat.format(RESOURCE_BUNDLE.getString(key), parameters);
    }

    /**
     * Permite la obtención del valor de la clave del archivo de propiedades general en formato <code>Integer</code>
     * 
     * @param key - Clave del archivo de propiedades que se desea obtener
     * @param parameters - Parámetros del mensaje
     * @return - Valor de la clave ingresada
     * @throws MissingResourceException
     */
    public static Integer getInteger(String key) throws MissingResourceException {
        return Integer.valueOf(RESOURCE_BUNDLE.getString(key));
    }
}
