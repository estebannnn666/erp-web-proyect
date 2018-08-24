package ec.com.erp.web.usuarios.datamanager;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import ec.com.erp.cliente.mdl.dto.UsuariosDTO;
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
public class UsuariosDataManager extends CommonDataManager implements Serializable {

	public static final String DATA_MANAGER_ID = "usuariosDataManager";

	private UsuariosDTO usuariosDTOEditar;
	 
	@ManagedProperty(value="#{sessionDataManagerBase}")
	private SessionDataManagerBase sessionDataManagerBase;
	
	@Override
	public String getIdDataManager() {
		return "usuariosDataManager";
	}

	public SessionDataManagerBase getSessionDataManagerBase() {
		return sessionDataManagerBase;
	}

	public void setSessionDataManagerBase(SessionDataManagerBase sessionDataManagerBase) {
		this.sessionDataManagerBase = sessionDataManagerBase;
	}

	public UsuariosDTO getUsuariosDTOEditar() {
		return usuariosDTOEditar;
	}

	public void setUsuariosDTOEditar(UsuariosDTO usuariosDTOEditar) {
		this.usuariosDTOEditar = usuariosDTOEditar;
	}
}
