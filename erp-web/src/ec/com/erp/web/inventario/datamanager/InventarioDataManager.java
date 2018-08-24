package ec.com.erp.web.inventario.datamanager;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import ec.com.erp.cliente.mdl.dto.InventarioDTO;
import ec.com.erp.web.commons.datamanager.CommonDataManager;
import ec.com.erp.web.commons.datamanager.SessionDataManagerBase;

/**
 * 
 * @author hgudino
 *
 */
@ManagedBean
@SessionScoped
@SuppressWarnings("serial")
public class InventarioDataManager extends CommonDataManager implements Serializable {

	public static final String DATA_MANAGER_ID = "inventarioDataManager";

	private InventarioDTO inventarioDTOEditar;
	 
	@ManagedProperty(value="#{sessionDataManagerBase}")
	private SessionDataManagerBase sessionDataManagerBase;
	
	@Override
	public String getIdDataManager() {
		return "inventarioDataManager";
	}

	public SessionDataManagerBase getSessionDataManagerBase() {
		return sessionDataManagerBase;
	}

	public void setSessionDataManagerBase(SessionDataManagerBase sessionDataManagerBase) {
		this.sessionDataManagerBase = sessionDataManagerBase;
	}

	public InventarioDTO getInventarioDTOEditar() {
		return inventarioDTOEditar;
	}

	public void setInventarioDTOEditar(InventarioDTO inventarioDTOEditar) {
		this.inventarioDTOEditar = inventarioDTOEditar;
	}
}
