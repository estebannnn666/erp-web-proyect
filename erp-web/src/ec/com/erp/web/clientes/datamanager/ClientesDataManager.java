package ec.com.erp.web.clientes.datamanager;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import ec.com.erp.cliente.mdl.dto.ClienteDTO;
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
public class ClientesDataManager extends CommonDataManager implements Serializable {

	public static final String DATA_MANAGER_ID = "clientesDataManager";
	
	private ClienteDTO clienteDTOEditar;

	@ManagedProperty(value="#{sessionDataManagerBase}")
	private SessionDataManagerBase sessionDataManagerBase;
	
	@Override
	public String getIdDataManager() {
		return "clientesDataManager";
	}

	public SessionDataManagerBase getSessionDataManagerBase() {
		return sessionDataManagerBase;
	}

	public void setSessionDataManagerBase(SessionDataManagerBase sessionDataManagerBase) {
		this.sessionDataManagerBase = sessionDataManagerBase;
	}

	public ClienteDTO getClienteDTOEditar() {
		return clienteDTOEditar;
	}

	public void setClienteDTOEditar(ClienteDTO clienteDTOEditar) {
		this.clienteDTOEditar = clienteDTOEditar;
	}
}
