package ec.com.erp.web.modulos.datamanager;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import ec.com.erp.cliente.mdl.dto.ModuloDTO;
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
public class ModulosDataManager extends CommonDataManager implements Serializable {

	public static final String DATA_MANAGER_ID = "modulosDataManager";

	private ModuloDTO moduloDTOEditar;
	 
	@ManagedProperty(value="#{sessionDataManagerBase}")
	private SessionDataManagerBase sessionDataManagerBase;
	
	@Override
	public String getIdDataManager() {
		return "modulosDataManager";
	}

	public SessionDataManagerBase getSessionDataManagerBase() {
		return sessionDataManagerBase;
	}

	public void setSessionDataManagerBase(SessionDataManagerBase sessionDataManagerBase) {
		this.sessionDataManagerBase = sessionDataManagerBase;
	}

	public ModuloDTO getModuloDTOEditar() {
		return moduloDTOEditar;
	}

	public void setModuloDTOEditar(ModuloDTO moduloDTOEditar) {
		this.moduloDTOEditar = moduloDTOEditar;
	}
}
