package ec.com.erp.web.transportista.datamanager;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import ec.com.erp.cliente.mdl.dto.TransportistaDTO;
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
public class TransportistaDataManager extends CommonDataManager implements Serializable {

	public static final String DATA_MANAGER_ID = "transportistaDataManager";
	
	private TransportistaDTO transportistaDTOEditar;
	
	@ManagedProperty(value="#{sessionDataManagerBase}")
	private SessionDataManagerBase sessionDataManagerBase;
	
	@Override
	public String getIdDataManager() {
		return "transportistaDataManager";
	}

	public SessionDataManagerBase getSessionDataManagerBase() {
		return sessionDataManagerBase;
	}

	public void setSessionDataManagerBase(SessionDataManagerBase sessionDataManagerBase) {
		this.sessionDataManagerBase = sessionDataManagerBase;
	}

	public TransportistaDTO getTransportistaDTOEditar() {
		return transportistaDTOEditar;
	}

	public void setTransportistaDTOEditar(TransportistaDTO transportistaDTOEditar) {
		this.transportistaDTOEditar = transportistaDTOEditar;
	}
}
