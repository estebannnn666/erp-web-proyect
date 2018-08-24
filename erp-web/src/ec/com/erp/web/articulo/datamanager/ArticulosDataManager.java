package ec.com.erp.web.articulo.datamanager;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import ec.com.erp.cliente.mdl.dto.ArticuloDTO;
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
public class ArticulosDataManager extends CommonDataManager implements Serializable {

	public static final String DATA_MANAGER_ID = "articulosDataManager";

	private ArticuloDTO articuloDTOEditar;
	 
	@ManagedProperty(value="#{sessionDataManagerBase}")
	private SessionDataManagerBase sessionDataManagerBase;
	
	@Override
	public String getIdDataManager() {
		return "articulosDataManager";
	}

	public SessionDataManagerBase getSessionDataManagerBase() {
		return sessionDataManagerBase;
	}

	public void setSessionDataManagerBase(SessionDataManagerBase sessionDataManagerBase) {
		this.sessionDataManagerBase = sessionDataManagerBase;
	}

	public ArticuloDTO getArticuloDTOEditar() {
		return articuloDTOEditar;
	}

	public void setArticuloDTOEditar(ArticuloDTO articuloDTOEditar) {
		this.articuloDTOEditar = articuloDTOEditar;
	}
}
