/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.tebowcurereports.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.tebowcurereports.reporting.SetupAccomplishmentsReport;
import org.openmrs.module.tebowcurereports.reporting.SetupSurgicalMemorandumPrintReport;
import org.openmrs.module.tebowcurereports.reporting.SetupHospitalMonthlyIndicatorsReport;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.ReportRequest.Status;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.service.ReportService;

import java.util.List;

/**
 *
 */
public class CleanReportingTablesAndRegisterAllReports {
	
	private static Log log = LogFactory.getLog(CleanReportingTablesAndRegisterAllReports.class);
	
	public static void cleanTables() throws Exception {
		
		ReportService rs = Context.getService(ReportService.class);
		List<ReportDesign> rDes = rs.getAllReportDesigns(true);
		for (ReportDesign reportDesign : rDes) {
			rs.purgeReportDesign(reportDesign);
		}
		
		ReportDefinitionService rds = Context.getService(ReportDefinitionService.class);
		List<ReportDefinition> rDefs = rds.getAllDefinitions(true);
		for (ReportDefinition reportDefinition : rDefs) {
			rds.purgeDefinition(reportDefinition);
		}
		
		for (ReportRequest request : rs.getReportRequests(null, null, null, Status.COMPLETED, Status.FAILED)) {
			try {
				rs.purgeReportRequest(request);
			}
			catch (Exception e) {
				log.warn("Unable to delete old report request: " + request, e);
			}
		}
	}
	
	public static void registerReports() throws Exception {
		new SetupAccomplishmentsReport().setup();
		new SetupHospitalMonthlyIndicatorsReport().setup();
		new SetupSurgicalMemorandumPrintReport().setup();
	}
	
}
