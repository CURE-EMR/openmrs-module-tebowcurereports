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

import org.openmrs.Concept;
import org.openmrs.PersonAttributeType;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.PersonAttributeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.VisitCohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.tebowcurereports.util.Indicators;

/**
 * @author Bailly RURANGIRWA
 */
public class SetupHospitalMonthlyIndicatorsReport {
	
	private VisitType Ipd;
	
	private List<VisitType> opdVisitTypes;
	
	private List<VisitType> allVisitTypes;
	
	private PersonAttributeType patientType;
	
	private Concept charityPatientType;
	
	private Concept privatePatientType;
	
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
		rd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		rd.addParameter(new Parameter("endDate", "End Date", Date.class));
		rd.setName("Hospital Monthly Indicators Report");
		rd.addDataSetDefinition(createBaseDataSet(), ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		Helper.saveReportDefinition(rd);
		return rd;
	}
	
	private CohortIndicatorDataSetDefinition createBaseDataSet() {
		CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
		dsd.setName("Hospital Monthly Indicators Report Data Set");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		createIndicators(dsd);
		return dsd;
	}
	
	private void createIndicators(CohortIndicatorDataSetDefinition dsd) {
		
		VisitCohortDefinition patientsWithOpdVisitsBetweenDates = new VisitCohortDefinition();
		patientsWithOpdVisitsBetweenDates.addParameter(new Parameter("createdOnOrAfter", "Created on or after", Date.class));
		patientsWithOpdVisitsBetweenDates.addParameter(new Parameter("createdOnOrBefore", "Created on or before", Date.class));
		patientsWithOpdVisitsBetweenDates.setVisitTypeList(opdVisitTypes);
		
		CohortIndicator patientsWithOpdVisitsBetweenDatesIndicator = Indicators.newCohortIndicator("patientsWithOpdVisitsBetweenDatesIndicator", patientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("createdOnOrAfter=${startDate},createdOnOrBefore=${endDate}"));
		dsd.addColumn("oPdVisits", "Outpatient Visits", new Mapped<CohortIndicator>(patientsWithOpdVisitsBetweenDatesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		VisitCohortDefinition patientsWithAnyVisitBeforeDate = new VisitCohortDefinition();
		patientsWithAnyVisitBeforeDate.addParameter(new Parameter("createdOnOrBefore", "Created on or before", Date.class));
		patientsWithAnyVisitBeforeDate.setVisitTypeList(allVisitTypes);
		
		CompositionCohortDefinition newPatients = new CompositionCohortDefinition();
		newPatients.setName("newPatients");
		newPatients.addParameter(new Parameter("createdOnOrAfter", "Created on or after", Date.class));
		newPatients.addParameter(new Parameter("createdOnOrBefore", "Created on or before", Date.class));
		newPatients.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("createdOnOrAfter=${createdOnOrAfter},createdOnOrBefore=${createdOnOrBefore}")));
		
		newPatients.getSearches().put("2", new Mapped<CohortDefinition>(patientsWithAnyVisitBeforeDate, ParameterizableUtil.createParameterMappings("createdOnOrBefore=${createdOnOrAfter}")));
		newPatients.setCompositionString("1 and (not 2)");
		
		CohortIndicator newPatientsIndicator = Indicators.newCohortIndicator("newPatientsIndicator", newPatients, ParameterizableUtil.createParameterMappings("createdOnOrAfter=${startDate},createdOnOrBefore=${endDate}"));
		
		dsd.addColumn("newPatients", "Initial/New", new Mapped<CohortIndicator>(newPatientsIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		CompositionCohortDefinition followUpPatients = new CompositionCohortDefinition();
		followUpPatients.setName("followUpPatients");
		followUpPatients.addParameter(new Parameter("createdOnOrAfter", "Created on or after", Date.class));
		followUpPatients.addParameter(new Parameter("createdOnOrBefore", "Created on or before", Date.class));
		followUpPatients.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("createdOnOrAfter=${createdOnOrAfter},createdOnOrBefore=${createdOnOrBefore}")));
		
		followUpPatients.getSearches().put("2", new Mapped<CohortDefinition>(patientsWithAnyVisitBeforeDate, ParameterizableUtil.createParameterMappings("createdOnOrBefore=${createdOnOrAfter}")));
		followUpPatients.setCompositionString("1 and 2");
		
		CohortIndicator followUpPatientsIndicator = Indicators.newCohortIndicator("followUpPatientsIndicator", followUpPatients, ParameterizableUtil.createParameterMappings("createdOnOrAfter=${startDate},createdOnOrBefore=${endDate}"));
		
		dsd.addColumn("followUpPatients", "Follow-up/Return", new Mapped<CohortIndicator>(followUpPatientsIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setName("male Patients");
		males.setMaleIncluded(true);
		males.setFemaleIncluded(false);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setName("female Patients");
		females.setMaleIncluded(false);
		females.setFemaleIncluded(true);
		
		// Male
		
		CompositionCohortDefinition malePatientsWithOpdVisitsBetweenDates = new CompositionCohortDefinition();
		malePatientsWithOpdVisitsBetweenDates.setName("malePatientsWithOpdVisitsBetweenDates");
		malePatientsWithOpdVisitsBetweenDates.addParameter(new Parameter("createdOnOrAfter", "Created on or after", Date.class));
		malePatientsWithOpdVisitsBetweenDates.addParameter(new Parameter("createdOnOrBefore", "Created on or before", Date.class));
		malePatientsWithOpdVisitsBetweenDates.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("createdOnOrAfter=${createdOnOrAfter},createdOnOrBefore=${createdOnOrBefore}")));
		
		malePatientsWithOpdVisitsBetweenDates.getSearches().put("2", new Mapped<CohortDefinition>(males, null));
		malePatientsWithOpdVisitsBetweenDates.setCompositionString("1 and 2");
		
		CohortIndicator malePatientsWithOpdVisitsBetweenDatesIndicator = Indicators.newCohortIndicator("malePatientsWithOpdVisitsBetweenDatesIndicator", malePatientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("createdOnOrAfter=${startDate},createdOnOrBefore=${endDate}"));
		
		dsd.addColumn("malePatients", "Male", new Mapped<CohortIndicator>(malePatientsWithOpdVisitsBetweenDatesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		// Female
		
		CompositionCohortDefinition femalePatientsWithOpdVisitsBetweenDates = new CompositionCohortDefinition();
		femalePatientsWithOpdVisitsBetweenDates.setName("femalePatientsWithOpdVisitsBetweenDates");
		femalePatientsWithOpdVisitsBetweenDates.addParameter(new Parameter("createdOnOrAfter", "Created on or after", Date.class));
		femalePatientsWithOpdVisitsBetweenDates.addParameter(new Parameter("createdOnOrBefore", "Created on or before", Date.class));
		femalePatientsWithOpdVisitsBetweenDates.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("createdOnOrAfter=${createdOnOrAfter},createdOnOrBefore=${createdOnOrBefore}")));
		
		femalePatientsWithOpdVisitsBetweenDates.getSearches().put("2", new Mapped<CohortDefinition>(females, null));
		femalePatientsWithOpdVisitsBetweenDates.setCompositionString("1 and 2");
		
		CohortIndicator femalePatientsWithOpdVisitsBetweenDatesIndicator = Indicators.newCohortIndicator("femalePatientsWithOpdVisitsBetweenDatesIndicator", femalePatientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("createdOnOrAfter=${startDate},createdOnOrBefore=${endDate}"));
		
		dsd.addColumn("femalePatients", "Female", new Mapped<CohortIndicator>(femalePatientsWithOpdVisitsBetweenDatesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		CompositionCohortDefinition unknownGenderPatientsWithOpdVisitsBetweenDates = new CompositionCohortDefinition();
		unknownGenderPatientsWithOpdVisitsBetweenDates.setName("unknownGenderPatientsWithOpdVisitsBetweenDates");
		unknownGenderPatientsWithOpdVisitsBetweenDates.addParameter(new Parameter("createdOnOrAfter", "Created on or after", Date.class));
		unknownGenderPatientsWithOpdVisitsBetweenDates.addParameter(new Parameter("createdOnOrBefore", "Created on or before", Date.class));
		unknownGenderPatientsWithOpdVisitsBetweenDates.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("createdOnOrAfter=${createdOnOrAfter},createdOnOrBefore=${createdOnOrBefore}")));
		
		unknownGenderPatientsWithOpdVisitsBetweenDates.getSearches().put("2", new Mapped<CohortDefinition>(males, null));
		unknownGenderPatientsWithOpdVisitsBetweenDates.getSearches().put("3", new Mapped<CohortDefinition>(females, null));
		unknownGenderPatientsWithOpdVisitsBetweenDates.setCompositionString("1 and ( not 2) and ( not 3 )");
		
		CohortIndicator unknownGenderPatientsWithOpdVisitsBetweenDatesIndicator = Indicators.newCohortIndicator("unknownGenderPatientsWithOpdVisitsBetweenDatesIndicator", unknownGenderPatientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("createdOnOrAfter=${startDate},createdOnOrBefore=${endDate}"));
		
		dsd.addColumn("unknownGenderPatients", "Unknown Gender", new Mapped<CohortIndicator>(unknownGenderPatientsWithOpdVisitsBetweenDatesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		PersonAttributeCohortDefinition privatePatientCohortDefinition = new PersonAttributeCohortDefinition();
		privatePatientCohortDefinition.setAttributeType(patientType);
		List<Concept> privatePatientTypeConcepts = new ArrayList<Concept>();
		privatePatientTypeConcepts.add(privatePatientType);
		privatePatientCohortDefinition.setValueConcepts(privatePatientTypeConcepts);
		
		CompositionCohortDefinition privatePatientsWithOpdVisitsBetweenDates = new CompositionCohortDefinition();
		privatePatientsWithOpdVisitsBetweenDates.setName("privatePatientsWithOpdVisitsBetweenDates");
		privatePatientsWithOpdVisitsBetweenDates.addParameter(new Parameter("createdOnOrAfter", "Created on or after", Date.class));
		privatePatientsWithOpdVisitsBetweenDates.addParameter(new Parameter("createdOnOrBefore", "Created on or before", Date.class));
		privatePatientsWithOpdVisitsBetweenDates.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("createdOnOrAfter=${createdOnOrAfter},createdOnOrBefore=${createdOnOrBefore}")));
		
		privatePatientsWithOpdVisitsBetweenDates.getSearches().put("2", new Mapped<CohortDefinition>(privatePatientCohortDefinition, null));
		privatePatientsWithOpdVisitsBetweenDates.setCompositionString("1 and 2");
		
		CohortIndicator privatePatientsWithOpdVisitsBetweenDatesIndicator = Indicators.newCohortIndicator("privatePatientsWithOpdVisitsBetweenDatesIndicator", privatePatientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("createdOnOrAfter=${startDate},createdOnOrBefore=${endDate}"));
		
		dsd.addColumn("privatePatients", "Private", new Mapped<CohortIndicator>(privatePatientsWithOpdVisitsBetweenDatesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		PersonAttributeCohortDefinition charityPatientsCohortDefinition = new PersonAttributeCohortDefinition();
		charityPatientsCohortDefinition.setAttributeType(patientType);
		List<Concept> charityPatientTypeConcepts = new ArrayList<Concept>();
		charityPatientTypeConcepts.add(charityPatientType);
		charityPatientsCohortDefinition.setValueConcepts(charityPatientTypeConcepts);
		
		CompositionCohortDefinition charityPatientsWithOpdVisitsBetweenDates = new CompositionCohortDefinition();
		charityPatientsWithOpdVisitsBetweenDates.setName("charityPatientsWithOpdVisitsBetweenDates");
		charityPatientsWithOpdVisitsBetweenDates.addParameter(new Parameter("createdOnOrAfter", "Created on or after", Date.class));
		charityPatientsWithOpdVisitsBetweenDates.addParameter(new Parameter("createdOnOrBefore", "Created on or before", Date.class));
		charityPatientsWithOpdVisitsBetweenDates.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("createdOnOrAfter=${createdOnOrAfter},createdOnOrBefore=${createdOnOrBefore}")));
		
		charityPatientsWithOpdVisitsBetweenDates.getSearches().put("2", new Mapped<CohortDefinition>(charityPatientsCohortDefinition, null));
		charityPatientsWithOpdVisitsBetweenDates.setCompositionString("1 and 2");
		
		CohortIndicator charityPatientsWithOpdVisitsBetweenDatesIndicator = Indicators.newCohortIndicator("patientInYearRangeIndicator", charityPatientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("createdOnOrAfter=${startDate},createdOnOrBefore=${endDate}"));
		
		dsd.addColumn("charityPatients", "Charity", new Mapped<CohortIndicator>(charityPatientsWithOpdVisitsBetweenDatesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		CompositionCohortDefinition unknownPatientsWithOpdVisitsBetweenDates = new CompositionCohortDefinition();
		unknownPatientsWithOpdVisitsBetweenDates.setName("unknownPatientsWithOpdVisitsBetweenDates");
		unknownPatientsWithOpdVisitsBetweenDates.addParameter(new Parameter("createdOnOrAfter", "Created on or after", Date.class));
		unknownPatientsWithOpdVisitsBetweenDates.addParameter(new Parameter("createdOnOrBefore", "Created on or before", Date.class));
		unknownPatientsWithOpdVisitsBetweenDates.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("createdOnOrAfter=${createdOnOrAfter},createdOnOrBefore=${createdOnOrBefore}")));
		
		unknownPatientsWithOpdVisitsBetweenDates.getSearches().put("2", new Mapped<CohortDefinition>(charityPatientsCohortDefinition, null));
		unknownPatientsWithOpdVisitsBetweenDates.getSearches().put("3", new Mapped<CohortDefinition>(privatePatientCohortDefinition, null));
		unknownPatientsWithOpdVisitsBetweenDates.setCompositionString("1 and ( not 2 ) and ( not 3)");
		
		CohortIndicator uknownPatientsWithOpdVisitsBetweenDatesIndicator = Indicators.newCohortIndicator("uknownPatientsWithOpdVisitsBetweenDatesIndicator", unknownPatientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("createdOnOrAfter=${startDate},createdOnOrBefore=${endDate}"));
		
		dsd.addColumn("unknownPatientType", "Unknown Patient Type", new Mapped<CohortIndicator>(uknownPatientsWithOpdVisitsBetweenDatesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
	}
	
	private void setUpProperties() {
		Ipd = Context.getVisitService().getVisitTypeByUuid("c228eab1-3f10-11e4-adec-0800271c1b75");
		allVisitTypes = Context.getVisitService().getAllVisitTypes(false);
		opdVisitTypes = allVisitTypes;
		opdVisitTypes.remove(Ipd);
		
		patientType = Context.getPersonService().getPersonAttributeTypeByUuid("e9d5d7a4-8805-454f-ba60-e99f105fb2d1");
		privatePatientType = Context.getConceptService().getConceptByUuid("d12d6c6d-23d9-4164-9234-13154efe545a");
		charityPatientType = Context.getConceptService().getConceptByUuid("3a732c0e-0f07-49f4-a074-b3fb7a5526c6");
		
	}
	
}
