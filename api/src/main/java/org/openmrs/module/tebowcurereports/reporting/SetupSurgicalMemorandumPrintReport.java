package org.openmrs.module.tebowcurereports.reporting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

public class SetupSurgicalMemorandumPrintReport {
	
	private Concept formConcept;
	
	BuiltInEncounterDataLibrary encounterData = new BuiltInEncounterDataLibrary();
	
	private BasePatientDataLibrary basePatientData = new BasePatientDataLibrary();
	
	protected final static Log log = LogFactory.getLog(SetupSurgicalMemorandumPrintReport.class);
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	List<Concept> obsWeWant = null;
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		
		ReportDesign design = Helper.createRowPerPatientXlsOverviewReportDesign(rd, "surgicalMemorandum.xls", "surgicalMemorandum.xls_", null);
		
		Helper.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("Surgical Memorandum Report.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("Surgical Memorandum Report");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Surgical Memorandum Report");
		reportDefinition.addParameter(new Parameter("encounterUUID", "Encounter UUID", String.class));
		
		createDataSetDefinition(reportDefinition);
		
		Helper.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		
		FormPrintDataSetDefinition dsd = new FormPrintDataSetDefinition(formConcept);
		dsd.setName("dataSet");
		dsd.setParameters(getParameters());
		
		SqlEncounterQuery rowFilter = new SqlEncounterQuery();
		Parameter encounterUUID = new Parameter("encounterUUID", "Encounter UUID", String.class);
		encounterUUID.setRequired(true);
		
		rowFilter.addParameter(encounterUUID);
		
		rowFilter.setQuery("select encounter_id from encounter where uuid=:encounterUUID");
		
		MappedParametersEncounterQuery q = new MappedParametersEncounterQuery(rowFilter, ObjectUtil.toMap("encounterUUID=encounterUUID"));
		dsd.addRowFilter(Mapped.mapStraightThrough(q));
		
		dsd.addColumn("Family Name", basePatientData.getPreferredFamilyNames(), "");
		
		reportDefinition.addDataSetDefinition("dataSet", Mapped.mapStraightThrough(dsd));
		
	}
	
	public List<Parameter> getParameters() {
		List<Parameter> l = new ArrayList<Parameter>();
		l.add(new Parameter("encounterUUID", "Encounter UUID", String.class));
		return l;
	}
	
	private void setupProperties() {
		formConcept = MetadataLookup.getConcept("52acdbcb-ef5e-4413-91b7-2ada71858a68");
	}
	
}
