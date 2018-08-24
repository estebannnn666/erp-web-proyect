package ec.com.erp.web.despacho.datamanager;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import ec.com.erp.cliente.mdl.dto.GuiaDespachoDTO;
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
public class DespachoDataManager extends CommonDataManager implements Serializable {

	public static final String DATA_MANAGER_ID = "despachoDataManager";

	private GuiaDespachoDTO guiaDespachoDTOEditar;
	
	@ManagedProperty(value="#{sessionDataManagerBase}")
	private SessionDataManagerBase sessionDataManagerBase;
	
	@Override
	public String getIdDataManager() {
		return "despachoDataManager";
	}

	public SessionDataManagerBase getSessionDataManagerBase() {
		return sessionDataManagerBase;
	}

	public void setSessionDataManagerBase(SessionDataManagerBase sessionDataManagerBase) {
		this.sessionDataManagerBase = sessionDataManagerBase;
	}

	public GuiaDespachoDTO getGuiaDespachoDTOEditar() {
		return guiaDespachoDTOEditar;
	}

	public void setGuiaDespachoDTOEditar(GuiaDespachoDTO guiaDespachoDTOEditar) {
		this.guiaDespachoDTOEditar = guiaDespachoDTOEditar;
	}
}
