package org.openmrs.module.tebowcurereports.util;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition.TimeModifier;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.DateObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.MappedParametersCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.data.ConvertedDataDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.encounter.definition.ConvertedEncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PersonToPatientDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.query.encounter.definition.EncounterQuery;
import org.openmrs.module.reporting.query.encounter.definition.MappedParametersEncounterQuery;

public class Cohorts {
	
	public Log log = LogFactory.getLog(getClass());
	
	public static SqlCohortDefinition createPatientsNotVoided() {
		SqlCohortDefinition patientsNotVoided = new SqlCohortDefinition("select distinct p.patient_id from patient p where p.voided=0");
		return patientsNotVoided;
	}
	
	public static AgeCohortDefinition createXtoYAgeCohort(String name, int minAge, int maxAge) {
		AgeCohortDefinition xToYCohort = new AgeCohortDefinition();
		xToYCohort.setName(name);
		xToYCohort.setMaxAge(new Integer(maxAge));
		xToYCohort.setMinAge(new Integer(minAge));
		xToYCohort.addParameter(new Parameter("effectiveDate", "endDate", Date.class));
		return xToYCohort;
	}
	
	public static AgeCohortDefinition patientWithAgeBelow(int age) {
		AgeCohortDefinition patientsWithAgebelow = new AgeCohortDefinition();
		patientsWithAgebelow.setName("patientsWithAgebelow");
		patientsWithAgebelow.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
		patientsWithAgebelow.setMaxAge(age - 1);
		patientsWithAgebelow.setMaxAgeUnit(DurationUnit.YEARS);
		return patientsWithAgebelow;
	}
	
	public static AgeCohortDefinition patientWithAgeAbove(int age) {
		AgeCohortDefinition patientsWithAge = new AgeCohortDefinition();
		patientsWithAge.setName("patientsWithAge");
		patientsWithAge.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
		patientsWithAge.setMinAge(age);
		patientsWithAge.setMinAgeUnit(DurationUnit.YEARS);
		return patientsWithAge;
	}
	
	public static CohortDefinition getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDateAtLocation(Concept concept) {
		DateObsCohortDefinition cd = new DateObsCohortDefinition();
		cd.setTimeModifier(TimeModifier.ANY);
		cd.setQuestion(concept);
		cd.setOperator1(RangeComparator.GREATER_EQUAL);
		cd.addParameter(new Parameter("value1", "value1", Date.class));
		cd.setOperator2(RangeComparator.LESS_EQUAL);
		cd.addParameter(new Parameter("value2", "value2", Date.class));
		return convert(cd, ObjectUtil.toMap("value1=startDate,value2=endDate"));
	}
	
	public static CohortDefinition getPatientsWhoseObsValueDateIsBeforeEndDateAtLocation(Concept dateConcept) {
		DateObsCohortDefinition cd = new DateObsCohortDefinition();
		cd.setTimeModifier(TimeModifier.LAST);
		cd.setQuestion(dateConcept);
		cd.setOperator1(RangeComparator.LESS_EQUAL);
		cd.addParameter(new Parameter("value1", "value1", Date.class));
		return convert(cd, ObjectUtil.toMap("value1=endDate"));
	}
	
	public static SqlCohortDefinition getPatientsWithObservationsBetweenStartDateAndEndDate(String name, List<Concept> concepts) {
		SqlCohortDefinition obsBetweenStartDateAndEndDate = new SqlCohortDefinition();
		
		StringBuilder query = new StringBuilder("select distinct o.person_id from obs o where o.concept_id in (");
		int i = 0;
		for (Concept concept : concepts) {
			if (i > 0)
				query.append(",");
			query.append(concept.getId());
			i++;
		}
		query.append(") and o.voided=0 and o.obs_datetime>= :start and o.obs_datetime<= :end and o.value_numeric is NOT NULL");
		
		obsBetweenStartDateAndEndDate.setQuery(query.toString());
		obsBetweenStartDateAndEndDate.addParameter(new Parameter("startDate", "startDate", Date.class));
		obsBetweenStartDateAndEndDate.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		return obsBetweenStartDateAndEndDate;
	}
	
	public static CohortDefinition getAnyEncounterOfTypesDuringPeriod(List<EncounterType> types) {
		EncounterCohortDefinition cd = new EncounterCohortDefinition();
		cd.setEncounterTypeList(types);
		cd.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
		cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
		return cd;
	}
	
	// Convenience methods
	
	public static PatientDataDefinition convert(PatientDataDefinition pdd, Map<String, String> renamedParameters, DataConverter converter) {
		ConvertedPatientDataDefinition convertedDefinition = new ConvertedPatientDataDefinition();
		addAndConvertMappings(pdd, convertedDefinition, renamedParameters, converter);
		return convertedDefinition;
	}
	
	public static PatientDataDefinition convert(PatientDataDefinition pdd, DataConverter converter) {
		return convert(pdd, null, converter);
	}
	
	public static PatientDataDefinition convert(PersonDataDefinition pdd, Map<String, String> renamedParameters, DataConverter converter) {
		return convert(new PersonToPatientDataDefinition(pdd), renamedParameters, converter);
	}
	
	public static PatientDataDefinition convert(PersonDataDefinition pdd, DataConverter converter) {
		return convert(pdd, null, converter);
	}
	
	public static EncounterDataDefinition convert(EncounterDataDefinition pdd, Map<String, String> renamedParameters, DataConverter converter) {
		ConvertedEncounterDataDefinition convertedDefinition = new ConvertedEncounterDataDefinition();
		addAndConvertMappings(pdd, convertedDefinition, renamedParameters, converter);
		return convertedDefinition;
	}
	
	public static EncounterDataDefinition convert(EncounterDataDefinition pdd, DataConverter converter) {
		return convert(pdd, null, converter);
	}
	
	public static EncounterQuery convert(EncounterQuery query, Map<String, String> renamedParameters) {
		return new MappedParametersEncounterQuery(query, renamedParameters);
	}
	
	public static CohortDefinition convert(CohortDefinition cd, Map<String, String> renamedParameters) {
		return new MappedParametersCohortDefinition(cd, renamedParameters);
	}
	
	protected static <T extends DataDefinition> void addAndConvertMappings(T copyFrom, ConvertedDataDefinition<T> copyTo, Map<String, String> renamedParameters, DataConverter converter) {
		copyTo.setDefinitionToConvert(ParameterizableUtil.copyAndMap(copyFrom, copyTo, renamedParameters));
		if (converter != null) {
			copyTo.setConverters(Arrays.asList(converter));
		}
	}
}
