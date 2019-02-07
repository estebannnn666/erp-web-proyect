package ec.com.erp.web.converters.utils;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import ec.com.erp.cliente.mdl.dto.ArticuloDTO;
import ec.com.erp.web.pedidos.controller.ArticuloService;

@FacesConverter("articuloConverter")
public class ArticuloConverter implements Converter {
 
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
    	if(value != null && value.trim().length() > 0) {
            try {
            	ArticuloService service = (ArticuloService) context.getExternalContext().getApplicationMap().get("articuloService");
            	if(service != null){
            		return service.obtenerArticuloPorNombre(value);
            	}
            	else{
            		return null;
            	}
            } catch(NumberFormatException e) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not a valid theme."));
            }
        }
        else {
            return null;
        }
    }
    
    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
    	if(value != null) {
    		ArticuloDTO c = (ArticuloDTO) value;
    		return String.valueOf( c.getId().getCodigoArticulo() );
        }
        else {
            return null;
        }
    }
}
