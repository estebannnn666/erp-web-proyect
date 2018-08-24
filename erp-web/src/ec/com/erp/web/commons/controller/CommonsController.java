package ec.com.erp.web.commons.controller;

import java.io.Serializable;

import javax.faces.component.html.HtmlForm;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import ec.com.erp.web.commons.datamanager.CommonDataManager;
import ec.com.erp.web.commons.login.controller.LoginController;


/**
 * @author Esteban Gudino
 * 2017-07-13
 */
public abstract class CommonsController implements Serializable {

	private static final long serialVersionUID = 7765876811740798583L;
	
	private HtmlForm form;
	
	public abstract void initialize();
	
	public abstract CommonDataManager getDataManager();
	
	private boolean showMessagesBar = Boolean.FALSE;
	
	/**
	 * Indica si es el controlador original de la vista
	 */
	private Boolean isOriginal = Boolean.FALSE;

	/**
	 * Este método se ejecuta cada vez que la vista es cargada en su totalidad, por lo general esto ocurrirá cuando se ingresa por primera vez
	 * @return the form
	 */
	public HtmlForm getForm() {
		if(this.getDataManager()!= null){
			if(!this.getDataManager().getEstaInicializado()){				
				this.getDataManager().initializeCommonsData();
				//lamada al método que inicializa el controlador
				this.initialize();
				this.getDataManager().setEstaInicializado(Boolean.TRUE);	
			}
			if(this.getDataManager().getControllerAccessNumber() == 0){
				isOriginal = Boolean.TRUE;
			}
			this.getDataManager().setControllerAccessNumber(this.getDataManager().getControllerAccessNumber().intValue() + 1);
		}
		return form;
	}
	
	/**
	 * @param form the form to set
	 */
	public void setForm(HtmlForm form) {
		this.form = form;
	}

	/**
	 * Elimina las variables de sesión contenidas en el datamanager del controlador
	 */
	public void clearDataManager(ActionEvent event){
		LoginController sessionDataManagerBase = (LoginController)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("sessionDataManagerBase");	
		if(sessionDataManagerBase!= null && sessionDataManagerBase.isLoggedIn()){
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(getDataManager().getIdDataManager());
		}
	}
	
	/**
	 * Elimina el div de mensajes
	 * @param event
	 */
	public void limpiarMensajes(ActionEvent event){
		this.showMessagesBar = Boolean.FALSE;
	}
	
	/**
	 * Elimina el div de mensajes
	 * @param event
	 */
	public void activarMensajes(ActionEvent event){
		this.showMessagesBar = Boolean.TRUE;
	}

	public Boolean getIsOriginal() {
		return isOriginal;
	}

	public void setIsOriginal(Boolean isOriginal) {
		this.isOriginal = isOriginal;
	}
	
	public boolean isShowMessagesBar() {
		return showMessagesBar;
	}
	public void setShowMessagesBar(boolean showMessagesBar) {
		this.showMessagesBar = showMessagesBar;
	}
	
}
