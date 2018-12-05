package org.openmrs.module.tebowcurereports.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.tebowcurereports.reporting.SetupAccomplishmentsReport;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class CCHUSetupReportsFormController {
	
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
}
