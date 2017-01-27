package org.openmrs.addonindex.rest;

import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.service.Index;
import org.openmrs.addonindex.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Modules in the OpenMRS Maven Repository are named (module)-omod-(version).jar but upon download they need to be renamed
 * as (module)-version.omod. We aren't hosting these files within this application, so we can't use the `download` attribute
 * of a link to implement this. So instead we provide a way to download them by proxy, renaming along the way.
 * We limit this to only point to the OpenMRS Maven Repository to avoid abuse.
 */
@Controller
public class RenamingFileProxyController {
	
	@Autowired
	private RestTemplateBuilder restTemplateBuilder;
	
	@Autowired
	private Index index;
	
	@RequestMapping(method = RequestMethod.GET, value = "/api/v1/addon/{uid}/{version}/download")
	public void downloadWithCorrectName(@PathVariable("uid") String addonUid,
	                                    @PathVariable("version") String version,
	                                    HttpServletResponse response) throws Exception {
		
		AddOnInfoAndVersions addOn = index.getByUid(addonUid);
		if (addOn == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		Optional<AddOnVersion> addOnVersion = addOn.getVersion(new Version(version));
		if (!addOnVersion.isPresent()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		if (addOnVersion.get().getRenameTo() == null ||
				!addOnVersion.get().getDownloadUri().startsWith("http://mavenrepo.openmrs.org/nexus/")) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		Resource resource = restTemplateBuilder.build().getForObject(addOnVersion.get().getDownloadUri(), Resource.class);
		
		response.setHeader("Content-Disposition", "inline;filename=" + addOnVersion.get().getRenameTo());
		StreamUtils.copy(resource.getInputStream(), response.getOutputStream());
	}
	
}
