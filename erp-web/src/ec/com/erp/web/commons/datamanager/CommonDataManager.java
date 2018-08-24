/**
 * 
 */
package ec.com.erp.web.commons.datamanager;

import java.io.Serializable;

/**
 * Estructura generica
 * @author Esteban Gudino
 *
 */
@SuppressWarnings("serial")
public abstract class CommonDataManager implements Serializable {
	
	/**
	 * Obtiene el identificador del datamanager
	 * @return
	 */
	public abstract String getIdDataManager();
	
	/**
	 * Indica si una vista de usuario ya fue inicializada, si se desea inicializar nuevamente una vista del controlador se debera establecer en <code>false</code>
	 */
	private Boolean estaInicializado = Boolean.FALSE;
	
	/**
	 * variable que almacena la cantidad de veces que se ha instanciado el controlador con alcance View
	 */
	private Integer controllerAccessNumber = 0;
	
	/**
	 * Se inicializan datos comunes para la aplicacion
	 */
	public void initializeCommonsData(){		
	}
	
	/**
	 * @return the estaInicializado
	 */
	public Boolean getEstaInicializado() {
		return estaInicializado;
	}
	/**
	 * @param estaInicializado the estaInicializado to set
	 */
	public void setEstaInicializado(Boolean estaInicializado) {
		this.estaInicializado = estaInicializado;
	}
	
	/**
	 * @return the controllerAccessNumber
	 */
	public Integer getControllerAccessNumber() {
		return controllerAccessNumber;
	}
	/**
	 * @param controllerAccessNumber the controllerAccessNumber to set
	 */
	public void setControllerAccessNumber(Integer controllerAccessNumber) {
		this.controllerAccessNumber = controllerAccessNumber;
	}
	
}
