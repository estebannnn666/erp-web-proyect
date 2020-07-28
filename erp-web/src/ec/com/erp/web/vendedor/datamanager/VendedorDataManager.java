package ec.com.erp.web.vendedor.datamanager;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import ec.com.erp.cliente.mdl.dto.VendedorDTO;
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
public class VendedorDataManager extends CommonDataManager implements Serializable {

	public static final String DATA_MANAGER_ID = "vendedorDataManager";
	
	private VendedorDTO vendedorDTOEditar;
	
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

	public VendedorDTO getVendedorDTOEditar() {
		return vendedorDTOEditar;
	}

	public void setVendedorDTOEditar(VendedorDTO vendedorDTOEditar) {
		this.vendedorDTOEditar = vendedorDTOEditar;
	}
}
