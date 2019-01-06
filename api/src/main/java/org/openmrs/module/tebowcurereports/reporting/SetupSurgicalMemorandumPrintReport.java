package org.openmrs.module.tebowcurereports.reporting;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	
	protected final static Log log = LogFactory.getLog(SetupSurgicalMemorandumPrintReport.class);
	
	private Concept surgicalMemorandumGeneral;
	
	private Concept surgicalMemorandumWoundCare;
	
	private Concept surgicalMemorandumTenotomy;
	
	private Concept surgicalMemorandumPinsRemoval;
	
	BuiltInEncounterDataLibrary encounterData = new BuiltInEncounterDataLibrary();
	
	private BasePatientDataLibrary basePatientData = new BasePatientDataLibrary();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	List<Concept> obsWeWant = null;
	
	List<ReportDesign> reportDesigns = null;
	
	public void setup() throws Exception {
		
		reportDesigns = new ArrayList<ReportDesign>();
		Path path = Paths.get("/opt/bahmni-web/etc/bahmniapps/clinical/variables.js");
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		
		reportDesigns.add(Helper.createRowPerPatientXlsOverviewReportDesign(rd, "surgicalMemorandumGeneral.xls", "surgicalMemorandumGeneral.xls_", null));
		reportDesigns.add(Helper.createRowPerPatientXlsOverviewReportDesign(rd, "surgicalMemorandumWoundCare.xls", "surgicalMemorandumWoundCare.xls_", null));
		reportDesigns.add(Helper.createRowPerPatientXlsOverviewReportDesign(rd, "surgicalMemorandumTenotomy.xls", "surgicalMemorandumTenotomy.xls_", null));
		reportDesigns.add(Helper.createRowPerPatientXlsOverviewReportDesign(rd, "surgicalMemorandumPinsRemoval.xls", "surgicalMemorandumPinsRemoval.xls_", null));
		
		StringBuilder builder = new StringBuilder();
		builder.append("var formAndReporDesignMap = { \"");
		String prefix = "";
		for (ReportDesign reportDesign : reportDesigns) {
			ReportDesign savedReportDesign = Helper.saveReportDesign(reportDesign);
			String FormConceptUuid = getFormConcept(savedReportDesign).getUuid();
			String designUui = savedReportDesign.getUuid();
			
			builder.append(prefix);
			prefix = "\",\"";
			builder.append(FormConceptUuid + "\":\"" + designUui);
		}
		builder.append("\"};");
		builder.append("\n");
		builder.append("var reportDefinitionUuid = \"" + rd.getUuid() + "\";");
		Files.write(path, builder.toString().getBytes());
		
	}
	
	public void delete() {
		List<String> designs = Arrays.asList("surgicalMemorandumGeneral.xls_", "surgicalMemorandumWoundCare.xls_", "surgicalMemorandumTenotomy.xls_", "surgicalMemorandumPinsRemoval.xls_");
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if (designs.contains(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("Surgical Memorandum Report");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Surgical Memorandum Report");
		reportDefinition.addParameter(new Parameter("encounterUUID", "Encounter UUID", String.class));
		reportDefinition.addParameter(new Parameter("parentObsUuid", "Parent Obs UUID", String.class));
		
		createDataSetDefinition(reportDefinition);
		
		Helper.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		
		FormPrintDataSetDefinition dsd = new FormPrintDataSetDefinition();
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
		l.add(new Parameter("parentObsUuid", "Parent Obs UUID", String.class));
		return l;
	}
	
	private void setupProperties() {
		surgicalMemorandumGeneral = MetadataLookup.getConcept("3872");
		surgicalMemorandumWoundCare = MetadataLookup.getConcept("3956");
		surgicalMemorandumTenotomy = MetadataLookup.getConcept("3958");
		surgicalMemorandumPinsRemoval = MetadataLookup.getConcept("3965");
	}
	
	private Concept getFormConcept(ReportDesign reportDesign) {
		if (reportDesign.getName().equalsIgnoreCase("surgicalMemorandumGeneral.xls_")) {
			return surgicalMemorandumGeneral;
		}
		if (reportDesign.getName().equalsIgnoreCase("surgicalMemorandumWoundCare.xls_")) {
			return surgicalMemorandumWoundCare;
		}
		if (reportDesign.getName().equalsIgnoreCase("surgicalMemorandumTenotomy.xls_")) {
			return surgicalMemorandumTenotomy;
		}
		if (reportDesign.getName().equalsIgnoreCase("surgicalMemorandumPinsRemoval.xls_")) {
			return surgicalMemorandumPinsRemoval;
		}
		return null;
	}
	
}
