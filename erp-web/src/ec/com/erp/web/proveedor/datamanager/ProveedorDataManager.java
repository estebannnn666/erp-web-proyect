package ec.com.erp.web.proveedor.datamanager;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import ec.com.erp.cliente.mdl.dto.ProveedorDTO;
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
public class ProveedorDataManager extends CommonDataManager implements Serializable {

	public static final String DATA_MANAGER_ID = "proveedorDataManager";
	
	private ProveedorDTO proveedorDTOEditar;
	
	@ManagedProperty(value="#{sessionDataManagerBase}")
	private SessionDataManagerBase sessionDataManagerBase;
	
	@Override
	public String getIdDataManager() {
		return "proveedorDataManager";
	}

	public SessionDataManagerBase getSessionDataManagerBase() {
		return sessionDataManagerBase;
	}

	public void setSessionDataManagerBase(SessionDataManagerBase sessionDataManagerBase) {
		this.sessionDataManagerBase = sessionDataManagerBase;
	}

	public ProveedorDTO getProveedorDTOEditar() {
		return proveedorDTOEditar;
	}

	public void setProveedorDTOEditar(ProveedorDTO proveedorDTOEditar) {
		this.proveedorDTOEditar = proveedorDTOEditar;
	}
}
