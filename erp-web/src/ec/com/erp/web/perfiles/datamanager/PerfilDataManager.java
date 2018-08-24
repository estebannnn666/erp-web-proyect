package ec.com.erp.web.perfiles.datamanager;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import ec.com.erp.cliente.mdl.dto.PerfilDTO;
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
public class PerfilDataManager extends CommonDataManager implements Serializable {

	public static final String DATA_MANAGER_ID = "perfilDataManager";

	private PerfilDTO perfilDTOEditar;
	 
	@ManagedProperty(value="#{sessionDataManagerBase}")
	private SessionDataManagerBase sessionDataManagerBase;
	
	@Override
	public String getIdDataManager() {
		return "perfilDataManager";
	}

	public SessionDataManagerBase getSessionDataManagerBase() {
		return sessionDataManagerBase;
	}

	public void setSessionDataManagerBase(SessionDataManagerBase sessionDataManagerBase) {
		this.sessionDataManagerBase = sessionDataManagerBase;
	}

	public PerfilDTO getPerfilDTOEditar() {
		return perfilDTOEditar;
	}

	public void setPerfilDTOEditar(PerfilDTO perfilDTOEditar) {
		this.perfilDTOEditar = perfilDTOEditar;
	}
}
