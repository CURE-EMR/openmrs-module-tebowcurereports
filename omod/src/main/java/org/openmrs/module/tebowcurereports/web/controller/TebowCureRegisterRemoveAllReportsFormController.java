package org.openmrs.module.tebowcurereports.web.controller;

import org.openmrs.module.tebowcurereports.util.CleanReportingTablesAndRegisterAllReports;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class TebowCureRegisterRemoveAllReportsFormController {
	
	@RequestMapping("/module/tebowcurereports/register_allReports")
	public ModelAndView registerAllReports() throws Exception {
		CleanReportingTablesAndRegisterAllReports.registerReports();
		return new ModelAndView(new RedirectView("tebowcurereports.form"));
	}
	
	@RequestMapping("/module/tebowcurereports/remove_allReports")
	public ModelAndView removeAllReports() throws Exception {
		CleanReportingTablesAndRegisterAllReports.cleanTables();
		return new ModelAndView(new RedirectView("tebowcurereports.form"));
	}
	
}
