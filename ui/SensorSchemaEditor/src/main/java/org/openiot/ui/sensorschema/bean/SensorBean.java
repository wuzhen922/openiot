package org.openiot.ui.sensorschema.bean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;

import org.openiot.ui.sensorschema.rdf.OpeniotVocab;
import org.openiot.ui.sensorschema.rdf.SensorTypeSchema;


@ManagedBean
@SessionScoped
public class SensorBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4849861974062962596L;

	private List<ObservedPropertyBean> list;
    private transient DataModel<ObservedPropertyBean> model;
    private ObservedPropertyBean observation = new ObservedPropertyBean();
    private boolean edit;
    private String sensorType;
    private boolean output;
    private String pageTitle;
    private String outputText;
    private SensorTypeSchema ss;
    
    
    
    
    
    @PostConstruct
    public void init() {
    	//initialise the list for new observations that a sensor will measure
        list = new ArrayList<ObservedPropertyBean>();
        setPageTitle(OpeniotVocab.TITLE);
        output = false;
        sensorType = Constants.DISPLAYSTRING;
    }
    
    
    public void add() {

    	//we will add a dummy observation
    	observation.setObserves(Constants.DISPLAYSTRING);
    	observation.setFrequency(Constants.DISPLAYSTRING);
    	observation.setAccuracy(Constants.DISPLAYSTRING);    	
    	
    	observation.setId(list.isEmpty() ? 1 : list.get(list.size() - 1).getId() + 1);
        list.add(observation);
        observation = new ObservedPropertyBean(); // Reset placeholder.
    }

    public void edit() {
    	observation = model.getRowData();
        edit = true;
    }

    public void save() {
    	observation = new ObservedPropertyBean(); // Reset placeholder.
        edit = false;
    }
    
    public void delete() {
        list.remove(model.getRowData());
    }

    public List<ObservedPropertyBean> getList() {
        return list;
    }

    public DataModel<ObservedPropertyBean> getModel() {
        if (model == null) {
            model = new ListDataModel<ObservedPropertyBean>(list);
        }

        return model;
    }

    public ObservedPropertyBean getObservation() {
        return observation;
    }

    public boolean isEdit() {
        return edit;
    }


	public String getSensorType() {
		return sensorType;
	}


	public void setSensorType(String sensorType) {
		this.sensorType = sensorType;
	}
	
	public void generateRDF(){

		ss = new SensorTypeSchema();
		//check if a sensor with same name exists
		if (!ss.checkSensorTypeRegistration(sensorType)){
  
			//code to generate the RDF for the corresponding sensor type defined by the user
			
			ss.defineSensorType(sensorType);
			
			for (ObservedPropertyBean property: list){
				if (property.getObserves() != ""){
					ss.addObservedProperty(property.getObserves(), property.getAccuracy(), property.getFrequency());
				}				
			}
			
			this.outputText = ss.serializeRDF("N-TRIPLES");
			output = true;
		}
		else{
			
			addMessage("A sensor with same name exists in the system. \n Check the Sensor Instance Editor to create an Instance");
	        
		}
			
		
		
	}
	
	public void pushRDF(){
		boolean valid = true;
		
		if (output){			
			if (!ss.pushRdftoLSM(ss.serializeRDF("N-TRIPLES")))
			{
				valid = false;
				addMessage("Unable to register Sensor. Please check LSM configuration in openiot.properties");
		        
			}
		}
		else{
			valid = false;
			addMessage("Please click Generate Description and then click Register Sensor Type");
	        
		}
		
		if (valid)
		{
			addMessage("Sensor Registered Successfully");
	        
		}
			
	}
	
	public void clearSession(){

		//clear the session for new sensor type creation
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("/SensorSchemaEditor/SensorType.jsf");
		} catch (IOException e) {
			e.printStackTrace();
		}
		output = false;
	}
	

    public void addMessage(String summary) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary,  null);
        FacesContext.getCurrentInstance().addMessage("buttongrowl", message);
    }


	public boolean isOutput() {
		return output;
	}


	public void setOutput(boolean output) {
		this.output = output;
	}


	public String getPageTitle() {
		return pageTitle;
	}


	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}
	


	public String getOutputText() {
		return outputText;
	}


	public void setOutputText(String outputText) {
		this.outputText = outputText;
	}
}


