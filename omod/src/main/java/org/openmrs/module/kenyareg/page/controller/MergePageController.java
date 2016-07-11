package org.openmrs.module.kenyareg.page.controller;

import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

public class MergePageController {

	public String get(
			@RequestParam(value = "lpiUid", required = false) String lpiUid,
			@RequestParam(value = "mpiUid", required = false) String mpiUid,
			PageModel model, UiUtils ui) {
		if (lpiUid == null && mpiUid == null) {
			return "redirect:" + ui.pageLink("kenyareg", "basicSearch");
		}
		model.addAttribute("lpiUid", lpiUid);
		model.addAttribute("mpiUid", mpiUid);
		return null;
	}

}
