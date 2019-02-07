package ec.com.erp.web.pedidos.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import ec.com.erp.cliente.common.factory.ERPFactory;
import ec.com.erp.cliente.mdl.dto.ArticuloDTO;

@ManagedBean(name="articuloService", eager = true)
@ApplicationScoped
public class ArticuloService {
     
    private List<ArticuloDTO> articuloDTOCols;
     
    @PostConstruct
    public void init() {
    	articuloDTOCols = new ArrayList<ArticuloDTO>();
        Collection<ArticuloDTO> articuloCols = ERPFactory.articulos.getArticuloServicio().findObtenerListaArticulos(1, null, null);
		for (ArticuloDTO skin : articuloCols) {
            this.articuloDTOCols.add(skin);
        }
    }
    
    public ArticuloDTO obtenerArticuloPorNombre(String nombre){
    	Collection<ArticuloDTO> articuloCols = ERPFactory.articulos.getArticuloServicio().findObtenerListaArticulos(1, null, nombre);
    	if(articuloCols.isEmpty()){
    		return new ArticuloDTO();
    	}else{
    		return articuloCols.iterator().next();
    	}
    }

	public List<ArticuloDTO> getArticuloDTOCols() {
		return articuloDTOCols;
	}

	public void setArticuloDTOCols(List<ArticuloDTO> articuloDTOCols) {
		this.articuloDTOCols = articuloDTOCols;
	}
}