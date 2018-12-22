package org.openmrs.module.tebowcurereports.reporting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.data.encounter.library.BuiltInEncounterDataLibrary;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.query.encounter.definition.MappedParametersEncounterQuery;
import org.openmrs.module.reporting.query.encounter.definition.SqlEncounterQuery;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.tebowcurereports.dataset.definition.FormPrintDataSetDefinition;
import org.openmrs.module.tebowcurereports.reporting.library.BasePatientDataLibrary;
import org.openmrs.module.tebowcurereports.util.GlobalPropertiesManagement;
import org.openmrs.module.tebowcurereports.util.MetadataLookup;

public class SetupFormPrintReport {
	
	private Concept dateTimeOfAdmission;
	
	private Concept dateTimeOfDischarge;
	
	private Concept operationPerformed;
	
	private Concept finalBillingDiagnosis;
	
	BuiltInEncounterDataLibrary encounterData = new BuiltInEncounterDataLibrary();
	
	private BasePatientDataLibrary basePatientData = new BasePatientDataLibrary();
	
	protected final static Log log = LogFactory.getLog(SetupFormPrintReport.class);
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	List<Concept> obsWeWant = null;
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		
		ReportDesign design = Helper.createRowPerPatientXlsOverviewReportDesign(rd, "formPrintReport.xls", "formPrintReport.xls_", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:9,dataset:dataSet");
		props.put("sortWeight", "5000");
		design.setProperties(props);
		
		Helper.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("Form Print Report.xls_".equals(rd.getName()) || "Form Print Report.csv_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("Form Print Report");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Form Print Report");
		reportDefinition.addParameter(new Parameter("formName", "Form Name", Concept.class));
		reportDefinition.addParameter(new Parameter("encounterUUID", "Encounter UUID", String.class));
		
		createDataSetDefinition(reportDefinition);
		
		Helper.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		
		FormPrintDataSetDefinition dsd = new FormPrintDataSetDefinition(obsWeWant);
		dsd.setName("dataSet");
		dsd.setParameters(getParameters());
		
		SqlEncounterQuery rowFilter = new SqlEncounterQuery();
		rowFilter.addParameter(new Parameter("formName", "Form Name", Concept.class));
		
		Parameter encounterUUID = new Parameter("encounterUUID", "Encounter UUID", String.class);
		encounterUUID.setRequired(true);
		
		rowFilter.addParameter(encounterUUID);
		
		rowFilter.setQuery("select encounter_id from encounter where encounter_id=:encounterUUID");
		
		MappedParametersEncounterQuery q = new MappedParametersEncounterQuery(rowFilter, ObjectUtil.toMap("formName=formName,encounterUUID=encounterUUID"));
		dsd.addRowFilter(Mapped.mapStraightThrough(q));
		
		dsd.addColumn("Family Name", basePatientData.getPreferredFamilyNames(), "");
		
		reportDefinition.addDataSetDefinition("dataSet", Mapped.mapStraightThrough(dsd));
		
	}
	
	public List<Parameter> getParameters() {
		List<Parameter> l = new ArrayList<Parameter>();
		l.add(new Parameter("formName", "Form Name", Concept.class));
		l.add(new Parameter("encounterUUID", "Encounter UUID", String.class));
		return l;
	}
	
	private void setupProperties() {
		
		dateTimeOfAdmission = MetadataLookup.getConcept("3822");
		dateTimeOfDischarge = MetadataLookup.getConcept("3826");
		operationPerformed = MetadataLookup.getConcept("3834");
		finalBillingDiagnosis = MetadataLookup.getConcept("3651");
		
		obsWeWant = Arrays.asList(dateTimeOfAdmission, dateTimeOfDischarge, operationPerformed, finalBillingDiagnosis);
		
	}
	
}
