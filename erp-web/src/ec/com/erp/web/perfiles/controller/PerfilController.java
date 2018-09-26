
package ec.com.erp.web.perfiles.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import ec.com.erp.cliente.common.constantes.ERPConstantes;
import ec.com.erp.cliente.common.exception.ERPException;
import ec.com.erp.cliente.common.factory.ERPFactory;
import ec.com.erp.cliente.mdl.dto.CatalogoValorDTO;
import ec.com.erp.cliente.mdl.dto.ModuloDTO;
import ec.com.erp.cliente.mdl.dto.ModuloPerfilDTO;
import ec.com.erp.cliente.mdl.dto.PerfilDTO;
import ec.com.erp.web.commons.controller.CommonsController;
import ec.com.erp.web.commons.controller.MensajesController;
import ec.com.erp.web.commons.datamanager.CommonDataManager;
import ec.com.erp.web.commons.login.controller.LoginController;
import ec.com.erp.web.commons.utils.ERPWebResources;
import ec.com.erp.web.perfiles.datamanager.PerfilDataManager;

/**
 * Controlador para administracion de perfiles
 * @author hgudino
 *
 */
@ManagedBean
@ViewScoped
public class PerfilController extends CommonsController implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Data Managers
	@ManagedProperty(value="#{perfilDataManager}")
	private PerfilDataManager perfilDataManager;
	
	@ManagedProperty(value="#{loginController}")
	private LoginController loginController;
	
	// Variables
	private PerfilDTO perfilDTO;
	private Collection<PerfilDTO> perfilDTOCols;
	private Collection<ModuloDTO> modulosDTOCols;
	private Collection<ModuloPerfilDTO> modulosDTOAsignadosCols;
	private Collection<CatalogoValorDTO> tipoPerfilCatalogoValorDTOCols;
	private String nombrePerfil;
	private Integer page;
	private Boolean perfilCreado;

	@PostConstruct
	public void postConstruct() {
		this.perfilCreado = Boolean.FALSE;
		this.perfilDTO = new PerfilDTO();
		this.page = 0;
		modulosDTOCols = new ArrayList<ModuloDTO>();
		modulosDTOAsignadosCols = new ArrayList<ModuloPerfilDTO>();
		this.modulosDTOCols = ERPFactory.modulos.getModuloServicio().findObtenerListaModulosActivos(null);
		if(perfilDataManager.getPerfilDTOEditar() != null && perfilDataManager.getPerfilDTOEditar().getId().getCodigoPerfil() != null)
		{
			this.setPerfilDTO(perfilDataManager.getPerfilDTOEditar());
			if(CollectionUtils.isNotEmpty(perfilDataManager.getPerfilDTOEditar().getModuloPerfilDTOCols())){
				this.setModulosDTOAsignadosCols(perfilDataManager.getPerfilDTOEditar().getModuloPerfilDTOCols());
				for(ModuloPerfilDTO moduloPerfilDTO: modulosDTOAsignadosCols) {
					this.desactivarSeleccionoModulosQuitados(moduloPerfilDTO.getId().getCodigoModulo());
				}
			}
		}
		this.tipoPerfilCatalogoValorDTOCols = ERPFactory.catalogos.getCatalogoServicio().findObtenerCatalogoByTipo(ERPConstantes.CODIGO_CATALOGO_TIPOS_PERFILES);
		if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/modules/perfiles/adminBusquedaPerfil.xhtml")) {
			this.perfilDTOCols = ERPFactory.perfiles.getPerfilesServicio().findObtenerListaPerfiles(nombrePerfil);
		}
	}
		
	@Override
	public CommonDataManager getDataManager() {
		return perfilDataManager;
	}

	@Override
	public void initialize() {
		
	}

	@Override
	public void clearDataManager(ActionEvent event) {
		super.clearDataManager(event);
	}

	/**
	 * Metodo para buscar modulos
	 * @param e
	 */
	public void busquedaPerfiles(ActionEvent e){
		try {
			this.perfilDTOCols = ERPFactory.perfiles.getPerfilesServicio().findObtenerListaPerfiles(nombrePerfil);
			if(CollectionUtils.isEmpty(this.perfilDTOCols)){
				this.setShowMessagesBar(Boolean.TRUE);
				MensajesController.addInfo(null, ERPWebResources.getString("ec.com.erp.etiqueta.mensaje.lista.resultado"));
			}
		} catch (ERPException e1) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e1.getMessage());
		} catch (Exception e2) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e2.getMessage());
		}
	}
	
	/**
	 * Metodo para guardar o actualizar modulos
	 * @param e
	 */
	public void guadarActualizarPerfil(ActionEvent e){
		try {
			this.setPerfilCreado(Boolean.FALSE);
			if(this.validarPantallaPerfil()) {
				this.perfilDTO.setCodigoTipoPerfil(ERPConstantes.CODIGO_CATALOGO_TIPOS_PERFILES);
				this.perfilDTO.setModuloPerfilDTOCols(this.modulosDTOAsignadosCols);
				this.perfilDTO.setNombrePerfil(this.perfilDTO.getNombrePerfil().toUpperCase());
				if(StringUtils.isNotEmpty(this.perfilDTO.getDescripcion())) {
					this.perfilDTO.setDescripcion(this.perfilDTO.getDescripcion().toUpperCase());
				}
				ERPFactory.perfiles.getPerfilesServicio().transCrearActualizarPerfil(this.perfilDTO);
				this.setShowMessagesBar(Boolean.TRUE);
				this.setPerfilCreado(Boolean.TRUE);
				MensajesController.addInfo(null, "El m\u00F3dulo se cre\u00F3 correctamente.");
			}else{
				this.setShowMessagesBar(Boolean.TRUE);
			}
		} catch (ERPException e1) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e1.getMessage());
		} catch (Exception e2) {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, e2.getMessage());
		}
		
	}
	
	/**
	 * Validar datos ingresados
	 * @return
	 */
	public Boolean validarPantallaPerfil() {
		Boolean validado = Boolean.TRUE;
		if(StringUtils.isEmpty(perfilDTO.getNombrePerfil())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.pantalla.perfiles.nombre.requerido"));
			validado = Boolean.FALSE;
		}
		if(StringUtils.isEmpty(perfilDTO.getCodigoValorTipoPerfil())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.pantalla.perfiles.tipo.requerido"));
			validado = Boolean.FALSE;
		}
		if(StringUtils.isEmpty(perfilDTO.getEstado())){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.pantalla.perfiles.estado.requerido"));
			validado = Boolean.FALSE;
		}
		if(CollectionUtils.isEmpty(this.modulosDTOAsignadosCols)){
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.pantalla.perfiles.modulos.asignados.requerido"));
			validado = Boolean.FALSE;
		}
		return validado;
	}
	
	/**
	 * Metodo borrar pantalla y crear un perfil nuevo
	 * @param e
	 */
	public void clearNuevoPerfil(ActionEvent e){
		this.setPerfilCreado(Boolean.FALSE);
		this.setShowMessagesBar(Boolean.FALSE);
		this.perfilDTO = new PerfilDTO();
		this.perfilDataManager.setPerfilDTOEditar(new PerfilDTO());
	}
	
	/**
	 * Metodo para cargar datos a editar
	 * @return
	 */
	public String  cargarPerfil() {
		if(this.perfilDTO == null) {
			return null;
		}else{
			this.perfilDataManager.setPerfilDTOEditar(this.perfilDTO);
			return "/modules/perfiles/nuevoPerfil.xhtml?faces-redirect=true";
		}
	}
	
	/**
	 * Metodo para refrescar pantalla
	 * @param e
	 */
	public void refrescarPantalla(ActionEvent e){
		System.out.println("Ingreso a refrescar pantalla");
	}
	
	/**
	 * Metodo para regresar a la busqueda de perfiles
	 * @param e
	 */
	public String regresarBusquedaPerfil(){
		this.perfilDataManager.setPerfilDTOEditar(new PerfilDTO());
		return "/modules/perfiles/adminBusquedaPerfil.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para ir a la pantalla de nuevo perfil
	 * @return
	 */
	public String crearNuevoPerfil(){
		return "/modules/perfiles/nuevoPerfil.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para ir a la pantalla menu principal
	 * @return
	 */
	public String regresarMenuPrincipal(){
		return "/modules/principal/menu.xhtml?faces-redirect=true";
	}
	
	/**
	 * Metodo para agregar modulos al perfil
	 * @param e
	 */
	public void agregarModulos(ActionEvent e) {
		if(this.validarModulosSeleccionados()) {
			ModuloPerfilDTO moduloPerfilDTO;
			for(ModuloDTO moduloDTO: modulosDTOCols) {
				if(moduloDTO.getSeleccionado() != null && moduloDTO.getSeleccionado() ) {
					moduloPerfilDTO = new ModuloPerfilDTO();
					moduloPerfilDTO.getId().setCodigoModulo(moduloDTO.getId().getCodigoModulo());
					moduloPerfilDTO.setModuloDTO(moduloDTO);
					this.modulosDTOAsignadosCols.add(moduloPerfilDTO);
					this.desactivarSeleccionoModulosQuitados(moduloPerfilDTO.getId().getCodigoModulo());
				}
			}
			ArrayList<ModuloPerfilDTO> moduloPerfilDTOCols = new ArrayList<ModuloPerfilDTO>();
			moduloPerfilDTOCols.addAll(modulosDTOAsignadosCols);
			Collections.sort(moduloPerfilDTOCols, new Comparator<ModuloPerfilDTO>() {
				@Override
				public int compare(ModuloPerfilDTO p1, ModuloPerfilDTO p2) {
					return new Integer(p1.getModuloDTO().getOrden()).compareTo(new Integer(p2.getModuloDTO().getOrden()));
				}
			});
			
			modulosDTOAsignadosCols = new ArrayList<ModuloPerfilDTO>();
			modulosDTOAsignadosCols.addAll(moduloPerfilDTOCols);
		}
		else {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.pantalla.perfiles.modulos.asignados.requerido"));
		}
	}
	
	/**
	 * Metodo para quitar modulos del perfil
	 * @param e
	 */
	public void quitarModulos(ActionEvent e) {
		if(this.validarModulosAsignadosSeleccionados()) {
			Collection<ModuloPerfilDTO> moduloPerfilDTOTem = new ArrayList<ModuloPerfilDTO>();
			for(ModuloPerfilDTO moduloPerfilDTO: modulosDTOAsignadosCols) {
				if(moduloPerfilDTO.getSeleccionado() != null && moduloPerfilDTO.getSeleccionado()) {
					this.activarSeleccionoModulosQuitados(moduloPerfilDTO.getId().getCodigoModulo());
				}else {
					moduloPerfilDTOTem.add(moduloPerfilDTO);
				}
			}
			this.setModulosDTOAsignadosCols(moduloPerfilDTOTem);
		}
		else {
			this.setShowMessagesBar(Boolean.TRUE);
			MensajesController.addError(null, ERPWebResources.getString("ec.com.erp.etiqueta.pantalla.perfiles.modulos.asignados.requerido"));
		}
	}
	
	/**
	 * Metodo para desactivar seleccion
	 * @param codigoModulo
	 */
	public void desactivarSeleccionoModulosQuitados(Long codigoModulo) {
		for(ModuloDTO moduloDTO: modulosDTOCols) {
			if(moduloDTO.getId().getCodigoModulo().longValue() == codigoModulo.longValue()) {
				moduloDTO.setMostrarPerfil(Boolean.FALSE);
				break;
			}
		}
	}
	
	/**
	 * Metodo para desactivar seleccion
	 * @param codigoModulo
	 */
	public void activarSeleccionoModulosQuitados(Long codigoModulo) {
		for(ModuloDTO moduloDTO: modulosDTOCols) {
			if(moduloDTO.getId().getCodigoModulo().longValue() == codigoModulo.longValue()) {
				moduloDTO.setMostrarPerfil(Boolean.TRUE);
				break;
			}
		}
	}
	
	/**
	 * Validar que existan elementos seleccionados al agregar
	 * @return
	 */
	public Boolean validarModulosSeleccionados() {
		Boolean resultado =  Boolean.FALSE;
		if(CollectionUtils.isNotEmpty(modulosDTOCols)) {
			for(ModuloDTO moduloDTO: modulosDTOCols) {
				if(moduloDTO.getSeleccionado() != null && moduloDTO.getSeleccionado()) {
					resultado = Boolean.TRUE;
					break;
				}
			}
		}
		return resultado;
	}
	
	/**
	 * Validar que existan elementos seleccionados al quitar
	 * @return
	 */
	public Boolean validarModulosAsignadosSeleccionados() {
		Boolean resultado =  Boolean.FALSE;
		if(CollectionUtils.isNotEmpty(modulosDTOAsignadosCols)) {
			for(ModuloPerfilDTO moduloPerfilDTO: modulosDTOAsignadosCols) {
				if(moduloPerfilDTO.getSeleccionado() != null && moduloPerfilDTO.getSeleccionado()) {
					resultado = Boolean.TRUE;
					break;
				}
			}
		}
		return resultado;
	}
	
	/**
	 * Borrar filtro de nombre de perfil
	 */
	public void borrarBusquedaNombrePerfil(ActionEvent e){
		this.nombrePerfil = "";
	}
	
	public void setPerfilDataManager(PerfilDataManager perfilDataManager) {
		this.perfilDataManager = perfilDataManager;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public LoginController getLoginController() {
		return loginController;
	}

	public void setLoginController(LoginController loginController) {
		this.loginController = loginController;
	}

	public PerfilDTO getPerfilDTO() {
		return perfilDTO;
	}

	public void setPerfilDTO(PerfilDTO perfilDTO) {
		this.perfilDTO = perfilDTO;
	}

	public Collection<PerfilDTO> getPerfilDTOCols() {
		return perfilDTOCols;
	}

	public void setPerfilDTOCols(Collection<PerfilDTO> perfilDTOCols) {
		this.perfilDTOCols = perfilDTOCols;
	}

	public String getNombrePerfil() {
		return nombrePerfil;
	}

	public void setNombrePerfil(String nombrePerfil) {
		this.nombrePerfil = nombrePerfil;
	}

	public Boolean getPerfilCreado() {
		return perfilCreado;
	}

	public void setPerfilCreado(Boolean perfilCreado) {
		this.perfilCreado = perfilCreado;
	}

	public Collection<ModuloDTO> getModulosDTOCols() {
		return modulosDTOCols;
	}

	public void setModulosDTOCols(Collection<ModuloDTO> modulosDTOCols) {
		this.modulosDTOCols = modulosDTOCols;
	}

	public Collection<ModuloPerfilDTO> getModulosDTOAsignadosCols() {
		return modulosDTOAsignadosCols;
	}

	public void setModulosDTOAsignadosCols(Collection<ModuloPerfilDTO> modulosDTOAsignadosCols) {
		this.modulosDTOAsignadosCols = modulosDTOAsignadosCols;
	}

	public Collection<CatalogoValorDTO> getTipoPerfilCatalogoValorDTOCols() {
		return tipoPerfilCatalogoValorDTOCols;
	}

	public void setTipoPerfilCatalogoValorDTOCols(Collection<CatalogoValorDTO> tipoPerfilCatalogoValorDTOCols) {
		this.tipoPerfilCatalogoValorDTOCols = tipoPerfilCatalogoValorDTOCols;
	}
}
