package ec.com.erp.web.chofer.datamanager;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import ec.com.erp.cliente.mdl.dto.ChoferDTO;
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
public class ChoferDataManager extends CommonDataManager implements Serializable {

	public static final String DATA_MANAGER_ID = "choferDataManager";

	private ChoferDTO choferDTOEditar;
	
	@ManagedProperty(value="#{sessionDataManagerBase}")
	private SessionDataManagerBase sessionDataManagerBase;
	
	@Override
	public String getIdDataManager() {
		return "choferDataManager";
	}

	public SessionDataManagerBase getSessionDataManagerBase() {
		return sessionDataManagerBase;
	}

	public void setSessionDataManagerBase(SessionDataManagerBase sessionDataManagerBase) {
		this.sessionDataManagerBase = sessionDataManagerBase;
	}

	public ChoferDTO getChoferDTOEditar() {
		return choferDTOEditar;
	}

	public void setChoferDTOEditar(ChoferDTO choferDTOEditar) {
		this.choferDTOEditar = choferDTOEditar;
	}
}
