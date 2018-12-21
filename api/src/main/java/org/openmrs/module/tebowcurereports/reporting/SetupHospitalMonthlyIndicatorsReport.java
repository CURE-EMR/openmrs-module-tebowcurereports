/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.tebowcurereports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.openmrs.Program;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.tebowcurereports.util.Cohorts;
import org.openmrs.module.tebowcurereports.util.Indicators;

/**
 * @author Bailly RURANGIRWA
 */
public class SetupHospitalMonthlyIndicatorsReport {
	
	private Program HIVProgram;
	
	private List<Program> HIVPrograms = new ArrayList<Program>();
	
	private VisitType UPECVisitType;
	
	public void setup() throws Exception {
		
		setUpProperties();
		
		ReportDefinition rd = createReportDefinition();
		ReportDesign design = Helper.createRowPerPatientXlsOverviewReportDesign(rd, "HospitalMonthlyIndicatorsReport.xls", "HospitalMonthlyIndicatorsReport.xls_", null);
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,dataset:Hospital Monthly Indicators Report Data Set");
		props.put("sortWeight", "5000");
		design.setProperties(props);
		Helper.saveReportDesign(design);
		
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("HospitalMonthlyIndicatorsReport.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("Hospital Monthly Indicators Report");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition rd = new ReportDefinition();
		rd.addParameter(new Parameter("reportingStartDate", "Start Date", Date.class));
		rd.addParameter(new Parameter("endDate", "End Date", Date.class));
		rd.setName("Hospital Monthly Indicators Report");
		rd.addDataSetDefinition(createBaseDataSet(), ParameterizableUtil.createParameterMappings("endDate=${endDate},reportingStartDate=${reportingStartDate}"));
		Helper.saveReportDefinition(rd);
		return rd;
	}
	
	private CohortIndicatorDataSetDefinition createBaseDataSet() {
		CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
		dsd.setName("Hospital Monthly Indicators Report Data Set");
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		createIndicators(dsd);
		return dsd;
	}
	
	private void createIndicators(CohortIndicatorDataSetDefinition dsd) {
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setName("male Patients");
		males.setMaleIncluded(true);
		males.setFemaleIncluded(false);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setName("female Patients");
		females.setMaleIncluded(false);
		females.setFemaleIncluded(true);
		
		AgeCohortDefinition PatientBelow1Year = patientWithAgeBelow(1);
		PatientBelow1Year.setName("PatientBelow1Year");
		AgeCohortDefinition PatientBetween1And9Years = Cohorts.createXtoYAgeCohort("PatientBetween1And9Years", 1, 9);
		AgeCohortDefinition PatientBetween10And14Years = Cohorts.createXtoYAgeCohort("PatientBetween10And14Years", 10, 14);
		AgeCohortDefinition PatientBetween15And19Years = Cohorts.createXtoYAgeCohort("PatientBetween15And19Years", 15, 19);
		AgeCohortDefinition PatientBetween20And24Years = Cohorts.createXtoYAgeCohort("PatientBetween20And24Years", 20, 24);
		AgeCohortDefinition PatientBetween25And29Years = Cohorts.createXtoYAgeCohort("PatientBetween25And29Years", 25, 29);
		AgeCohortDefinition PatientBetween30And34Years = Cohorts.createXtoYAgeCohort("PatientBetween30And34Years", 30, 34);
		AgeCohortDefinition PatientBetween35And39Years = Cohorts.createXtoYAgeCohort("PatientBetween35And39Years", 35, 39);
		AgeCohortDefinition PatientBetween40And49Years = Cohorts.createXtoYAgeCohort("PatientBetween40And49Years", 40, 49);
		AgeCohortDefinition PatientBetween50YearsAndAbove = patientWithAgeAbove(50);
		PatientBetween50YearsAndAbove.setName("PatientBetween50YearsAndAbove");
		
		ArrayList<AgeCohortDefinition> agesRange = new ArrayList<AgeCohortDefinition>();
		// agesRange.add(PatientBelow1Year);
		// agesRange.add(PatientBetween1And9Years);
		agesRange.add(PatientBetween10And14Years);
		agesRange.add(PatientBetween15And19Years);
		agesRange.add(PatientBetween20And24Years);
		agesRange.add(PatientBetween25And29Years);
		agesRange.add(PatientBetween30And34Years);
		agesRange.add(PatientBetween35And39Years);
		agesRange.add(PatientBetween40And49Years);
		agesRange.add(PatientBetween50YearsAndAbove);
		
		// Male and Female <1
		
		CohortIndicator patientBelow1YearIndicator = Indicators.newCohortIndicator("patientBelow1YearIndicator", PatientBelow1Year, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}"));
		
		dsd.addColumn("C1<1", "TX_CURR: Currently on ART: Patients below 1 year", new Mapped(patientBelow1YearIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		// Male and Female between 1 and 9 years
		
		CohortIndicator patientBetween1And9YearsIndicator = Indicators.newCohortIndicator("patientBetween1And9YearsIndicator", PatientBetween1And9Years, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}"));
		
		dsd.addColumn("C119", "TX_CURR: Currently on ART: Patients between 1 and 9 years", new Mapped(patientBetween1And9YearsIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		// Male
		
		int i = 2;
		for (AgeCohortDefinition ageCohort : agesRange) {
			CompositionCohortDefinition patientInYearRange = new CompositionCohortDefinition();
			patientInYearRange.setName("patientInYearRangeEnrolledInHIVStarted");
			patientInYearRange.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
			patientInYearRange.getSearches().put("1", new Mapped<CohortDefinition>(ageCohort, ParameterizableUtil.createParameterMappings("effectiveDate=${effectiveDate}")));
			patientInYearRange.getSearches().put("2", new Mapped<CohortDefinition>(males, null));
			patientInYearRange.setCompositionString("1 and 2");
			
			CohortIndicator patientInYearRangeIndicator = Indicators.newCohortIndicator("patientInYearRangeIndicator", patientInYearRange, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}"));
			
			dsd.addColumn("C1M" + i, "Males:TX_CURR: Currently on ART by age and sex: " + ageCohort.getName(), new Mapped(patientInYearRangeIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
			
			i++;
		}
		
		// Females
		int j = 2;
		for (AgeCohortDefinition ageCohort : agesRange) {
			CompositionCohortDefinition patientInYearRange = new CompositionCohortDefinition();
			patientInYearRange.setName("patientInYearRangeEnrolledInHIVStarted");
			patientInYearRange.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
			patientInYearRange.getSearches().put("1", new Mapped<CohortDefinition>(ageCohort, ParameterizableUtil.createParameterMappings("effectiveDate=${effectiveDate}")));
			patientInYearRange.getSearches().put("2", new Mapped<CohortDefinition>(females, null));
			patientInYearRange.setCompositionString("1 and 2");
			
			CohortIndicator patientInYearRangeIndicator = Indicators.newCohortIndicator("patientInYearRangeIndicator", patientInYearRange, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}"));
			
			dsd.addColumn("C1F" + j, "Females:TX_CURR: Currently on ART by age and sex: " + ageCohort.getName(), new Mapped(patientInYearRangeIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
			j++;
		}
		
		CompositionCohortDefinition allPatients = new CompositionCohortDefinition();
		allPatients.setName("patientInYearRangeEnrolledInHIVStarted");
		allPatients.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
		allPatients.getSearches().put("1", new Mapped<CohortDefinition>(males, null));
		allPatients.getSearches().put("2", new Mapped<CohortDefinition>(females, null));
		allPatients.setCompositionString("1 or 2");
		
		CohortIndicator allPatientsIndicator = Indicators.newCohortIndicator("patientInYearRangeIndicator", allPatients, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}"));
		
		dsd.addColumn("C1All", "TX_CURR: Currently on ART", new Mapped(allPatientsIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
	}
	
	private void setUpProperties() {
		HIVProgram = Context.getProgramWorkflowService().getProgram(1);
		HIVPrograms.add(HIVProgram);
		UPECVisitType = Context.getVisitService().getVisitTypeByUuid("a7c2aaf0-c4e5-4310-aa94-07c7fe6a331a");
	}
	
	private AgeCohortDefinition patientWithAgeBelow(int age) {
		AgeCohortDefinition patientsWithAgebelow = new AgeCohortDefinition();
		patientsWithAgebelow.setName("patientsWithAgebelow");
		patientsWithAgebelow.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
		patientsWithAgebelow.setMaxAge(age - 1);
		patientsWithAgebelow.setMaxAgeUnit(DurationUnit.YEARS);
		return patientsWithAgebelow;
	}
	
	private AgeCohortDefinition patientWithAgeAbove(int age) {
		AgeCohortDefinition patientsWithAge = new AgeCohortDefinition();
		patientsWithAge.setName("patientsWithAge");
		patientsWithAge.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
		patientsWithAge.setMinAge(age);
		patientsWithAge.setMinAgeUnit(DurationUnit.YEARS);
		return patientsWithAge;
	}
}
