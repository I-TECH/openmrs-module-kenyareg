package org.openmrs.module.kenyareg.page.controller;

import org.apache.commons.lang.StringUtils;
import org.go2itech.oecui.data.RequestResult;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.session.Session;
import org.springframework.web.bind.annotation.RequestParam;

public class MergePageController {

	public String get(
			@RequestParam(value = "lpiUid", required = false) String lpiUid,
			@RequestParam(value = "mpiUid", required = false) String mpiUid,
			PageModel model, UiUtils ui, Session session) {
		RequestResult lpiResult = session.getAttribute("lpiResult", RequestResult.class);
		RequestResult mpiResult = session.getAttribute("mpiResult", RequestResult.class);
		if (StringUtils.isNotBlank(lpiUid) && lpiResult == null 
				|| StringUtils.isNotBlank(mpiUid) && mpiResult == null) {
			return "redirect:" + ui.pageLinkWithoutContextPath("kenyareg", "registryHome", null);
		}
		model.addAttribute("lpiUid", lpiUid);
		model.addAttribute("mpiUid", mpiUid);
		return null;
	}

}
