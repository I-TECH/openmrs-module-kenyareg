package org.openmrs.module.kenyareg.helper;

import ke.go.moh.oec.Person;
import org.go2itech.oecui.data.RequestResult;
import org.go2itech.oecui.data.RequestResultPair;
import org.go2itech.oecui.data.Server;
import org.openmrs.module.kenyareg.api.RegistryService;
import org.openmrs.module.kenyareg.form.SearchForm;
import org.openmrs.module.kenyaui.form.ValidatingCommandObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.session.Session;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by gitahi on 20/07/16.
 */
@Component
public class SearchHelper {

    public RequestResultPair search(
            RegistryService registryService,
            Session session,
            SearchForm form,
            UiUtils ui) {
        ui.validate(form, (ValidatingCommandObject) form, null);
        Person query = form.getPerson();
        RequestResultPair resultPair = registryService.findPerson(form.getServer(), query);

        if (form.getServer() == Server.LPI) {
            session.setAttribute("lpiResult", resultPair.getLpiResult());
        }
        if (form.getServer() == Server.MPI) {
            session.setAttribute("mpiResult", resultPair.getMpiResult());
        }
        if (form.getServer() == Server.MPI_LPI) {
            session.setAttribute("lpiResult", resultPair.getLpiResult());
            session.setAttribute("mpiResult", resultPair.getMpiResult());
        }

        return resultPair;
    }

    public Integer accept(RegistryService registryService,
                          Session session,
                          String uuid) {
        Person fromMpi = null;
        List<Person> personList = (List<Person>) session.getAttribute("lpiResult", RequestResult.class).getData();
        for (Person person : personList) {
            if (person.getPersonGuid().equals(uuid)) {
                fromMpi = person;
                break;
            }
        }
        if (fromMpi == null) {
            return null;
        }
        return registryService.acceptPerson(fromMpi).getId();
    }
}
