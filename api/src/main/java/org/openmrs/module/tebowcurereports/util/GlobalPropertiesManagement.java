package org.openmrs.module.tebowcurereports.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.RelationshipType;
import org.openmrs.api.context.Context;

public class GlobalPropertiesManagement {
	
	protected final static Log log = LogFactory.getLog(GlobalPropertiesManagement.class);
	
	public Program getProgram(String globalPropertyName) {
		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
		return MetadataLookup.getProgram(globalProperty);
	}
	
	public PatientIdentifierType getPatientIdentifier(String globalPropertyName) {
		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
		return MetadataLookup.getPatientIdentifierType(globalProperty);
	}
	
	public Concept getConcept(String globalPropertyName) {
		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
		return MetadataLookup.getConcept(globalProperty);
	}
	
	public List<Concept> getConceptList(String globalPropertyName) {
		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
		return MetadataLookup.getConceptList(globalProperty);
	}
	
	public List<Concept> getConceptList(String globalPropertyName, String separator) {
		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
		return MetadataLookup.getConceptList(globalProperty, separator);
	}
	
	public List<Concept> getConceptsByConceptSet(String globalPropertyName) {
		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
		Concept c = MetadataLookup.getConcept(globalProperty);
		return Context.getConceptService().getConceptsByConceptSet(c);
	}
	
	public Form getForm(String globalPropertyName) {
		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
		return MetadataLookup.getForm(globalProperty);
	}
	
	public EncounterType getEncounterType(String globalPropertyName) {
		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
		return MetadataLookup.getEncounterType(globalProperty);
	}
	
	public List<EncounterType> getEncounterTypeList(String globalPropertyName, String separator) {
		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
		return MetadataLookup.getEncounterTypeList(globalProperty, separator);
	}
	
	public List<EncounterType> getEncounterTypeList(String globalPropertyName) {
		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
		return MetadataLookup.getEncounterTypeList(globalProperty);
	}
	
	public List<Form> getFormList(String globalPropertyName) {
		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
		return MetadataLookup.getFormList(globalProperty);
	}
	
	public List<Form> getFormList(String globalPropertyName, String separator) {
		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
		return MetadataLookup.getFormList(globalProperty, separator);
	}
	
	public RelationshipType getRelationshipType(String globalPropertyName) {
		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
		return MetadataLookup.getRelationshipType(globalProperty);
	}
	
	public ProgramWorkflow getProgramWorkflow(String globalPropertyName, String programName) {
		String programGp = Context.getAdministrationService().getGlobalProperty(programName);
		String workflowGp = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
		return MetadataLookup.getProgramWorkflow(programGp, workflowGp);
	}
	
	public ProgramWorkflowState getProgramWorkflowState(String globalPropertyName, String workflowName, String programName) {
		String programGp = Context.getAdministrationService().getGlobalProperty(programName);
		String workflowGp = Context.getAdministrationService().getGlobalProperty(workflowName);
		String stateGp = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
		return MetadataLookup.getProgramWorkflowState(programGp, workflowGp, stateGp);
		
	}
	
	public List<ProgramWorkflowState> getProgramWorkflowStateList(String globalPropertyName) {
		String programGp = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
		return MetadataLookup.getProgramWorkflowstateList(programGp);
		
	}
	
	public Map<Concept, Double> getVialSizes() {
		Map<Concept, Double> vialSizes = new HashMap<Concept, Double>();
		String vialGp = Context.getAdministrationService().getGlobalProperty("reports.vialSizes");
		String[] vials = vialGp.split(",");
		for (String vial : vials) {
			String[] v = vial.split(":");
			try {
				Concept drugConcept = MetadataLookup.getConcept(v[0]);
				Double size = Double.parseDouble(v[1]);
				vialSizes.put(drugConcept, size);
			}
			catch (Exception e) {
				throw new IllegalArgumentException("Unable to convert " + vial + " into a vial size Concept and Double", e);
			}
		}
		return vialSizes;
	}
	
	public Integer getGlobalPropertyAsInt(String globalPropertyName) {
		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
		return Integer.parseInt(globalProperty);
	}
	
	// Identifiers
	public final static String IMB_IDENTIFIER = "reports.imbIdIdentifier";
	
	// Concepts
	public final static String RETURN_VISIT_DATE = "concept.returnVisitDate";
	
	// Encounters
	public final static String ADULT_FLOWSHEET_ENCOUNTER = "reports.adultflowsheetencounter";
	
	// RelationshipTypes
	public final static String ACCOMPAGNATUER_RELATIONSHIP = "reports.accompagnatuerRelationship";
	
	public final static String MOTHER_RELATIONSHIP = "reports.pmtctMotherRelationship";
	
	// Forms
	public final static String CARDIOLOGY_CONSULT_FORM = "cardiologyreporting.cardilogyConsultationFormId";
	
	// Lab Panel Concepts
	public final static String CD4_PANEL_LAB_CONCEPT = "reports.cd4LabConcept";
	
	// Order types
	public final static String LAB_ORDER_TYPE = "reports.labOrderType";
	
	// Drug
	public final static String BACTRIM = "reports.BactrimDrug";
	
}
