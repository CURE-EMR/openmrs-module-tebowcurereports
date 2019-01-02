package org.openmrs.module.tebowcurereports.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.tebowcurereports.reporting.SetupAccomplishmentsReport;
import org.openmrs.module.tebowcurereports.reporting.SetupSurgicalMemorandumPrintReport;
import org.openmrs.module.tebowcurereports.reporting.SetupHospitalMonthlyIndicatorsReport;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class TebowCureSetupReportsFormController {
	
	public Log log = LogFactory.getLog(getClass());
	
	@RequestMapping(value = "/module/tebowcurereports/tebowcurereports", method = RequestMethod.GET)
	public void manage() {
	}
	
	@RequestMapping("/module/tebowcurereports/register_accomplishmentsReport")
	public ModelAndView registerAccomplishmentsReport() throws Exception {
		new SetupAccomplishmentsReport().setup();
		return new ModelAndView(new RedirectView("tebowcurereports.form"));
	}
	
	@RequestMapping("/module/tebowcurereports/remove_accomplishmentsReport")
	public ModelAndView removeAccomplishmentsReport() throws Exception {
		new SetupAccomplishmentsReport().delete();
		return new ModelAndView(new RedirectView("tebowcurereports.form"));
	}
	
	@RequestMapping("/module/tebowcurereports/register_hospitalMonthlyIndicatorsReport")
	public ModelAndView registerHospitalMonthlyIndicatorsReport() throws Exception {
		new SetupHospitalMonthlyIndicatorsReport().setup();
		return new ModelAndView(new RedirectView("tebowcurereports.form"));
	}
	
	@RequestMapping("/module/tebowcurereports/remove_hospitalMonthlyIndicatorsReport")
	public ModelAndView removeHospitalMonthlyIndicatorsReport() throws Exception {
		new SetupHospitalMonthlyIndicatorsReport().delete();
		return new ModelAndView(new RedirectView("tebowcurereports.form"));
	}
	
	@RequestMapping("/module/tebowcurereports/register_formPrintReport")
	public ModelAndView registerFormPrintReport() throws Exception {
		new SetupSurgicalMemorandumPrintReport().setup();
		return new ModelAndView(new RedirectView("tebowcurereports.form"));
	}
	
	@RequestMapping("/module/tebowcurereports/remove_formPrintReport")
	public ModelAndView removeFormPrintReport() throws Exception {
		new SetupSurgicalMemorandumPrintReport().delete();
		return new ModelAndView(new RedirectView("tebowcurereports.form"));
	}
}
