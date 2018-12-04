package org.openmrs.module.tebowcurereports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.data.encounter.library.BuiltInEncounterDataLibrary;
import org.openmrs.module.reporting.dataset.definition.EncounterAndObsDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.query.encounter.definition.MappedParametersEncounterQuery;
import org.openmrs.module.reporting.query.encounter.definition.SqlEncounterQuery;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.tebowcurereports.reporting.library.BasePatientDataLibrary;
import org.openmrs.module.tebowcurereports.util.GlobalPropertiesManagement;

public class SetupAccomplishmentsReport {
	
	BuiltInEncounterDataLibrary encounterData = new BuiltInEncounterDataLibrary();
	
	private BasePatientDataLibrary basePatientData = new BasePatientDataLibrary();
	
	protected final static Log log = LogFactory.getLog(SetupAccomplishmentsReport.class);
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	public void setup() throws Exception {
		ReportDefinition rd = createReportDefinition();
		ReportDesign designExcel = Helper.createExcelDesign(rd, "Doctors Accomplishments Report.xls_", true);
		
		ReportDesign designCSV = Helper.createCsvReportDesign(rd, "Doctors Accomplishments Report.csv_");
		
		Helper.saveReportDesign(designExcel);
		Helper.saveReportDesign(designCSV);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("Doctors Accomplishments Report.xls_".equals(rd.getName()) || "Doctors Accomplishments Report.csv_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("Doctors Accomplishments Report");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Doctors Accomplishments Report");
		reportDefinition.addParameter(new Parameter("startDate", "From Date", Date.class));
		reportDefinition.addParameter(new Parameter("endDate", "To Date", Date.class));
		
		Parameter form = new Parameter("doctor", "Doctor", Concept.class);
		
		form.setRequired(false);
		
		reportDefinition.addParameter(form);
		
		createDataSetDefinition(reportDefinition);
		
		Helper.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		EncounterAndObsDataSetDefinition dsd = new EncounterAndObsDataSetDefinition();
		dsd.setName("dsd");
		dsd.setParameters(getParameters());
		
		SqlEncounterQuery rowFilter = new SqlEncounterQuery();
		rowFilter.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		rowFilter.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		Parameter doctor = new Parameter("doctor", "Doctor", Concept.class);
		doctor.setRequired(false);
		
		rowFilter.addParameter(doctor);
		
		rowFilter.setQuery("select encounter_id from obs where value_coded=:doctor and obs_datetime >=:onOrAfter and   obs_datetime <=:onOrBefore and voided=0 ");
		
		MappedParametersEncounterQuery q = new MappedParametersEncounterQuery(rowFilter, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=endDate,doctor=doctor"));
		dsd.addRowFilter(Mapped.mapStraightThrough(q));
		
		dsd.addColumn("Family Name", basePatientData.getPreferredFamilyNames(), "");
		
		reportDefinition.addDataSetDefinition("dsd", Mapped.mapStraightThrough(dsd));
		
	}
	
	public List<Parameter> getParameters() {
		List<Parameter> l = new ArrayList<Parameter>();
		l.add(new Parameter("startDate", "From Date", Date.class));
		l.add(new Parameter("endDate", "To Date", Date.class));
		
		Parameter encouterType = new Parameter("encounterTypes", "Encounter Type", EncounterType.class);
		Parameter form = new Parameter("doctor", "Doctor", Concept.class);
		
		encouterType.setRequired(false);
		form.setRequired(false);
		
		l.add(encouterType);
		l.add(form);
		return l;
	}
	
}
