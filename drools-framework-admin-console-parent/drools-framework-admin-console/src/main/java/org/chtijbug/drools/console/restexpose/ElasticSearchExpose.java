package org.chtijbug.drools.console.restexpose;

import io.swagger.annotations.Api;
import org.chtijbug.drools.console.service.IndexerService;
import org.chtijbug.drools.proxy.persistence.model.BusinessTransactionAction;
import org.chtijbug.drools.proxy.persistence.model.BusinessTransactionPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api/logging")
@Api("/api/logging")
public class ElasticSearchExpose {

    @Autowired
    private IndexerService indexerService;

    @GetMapping(value = "/findLastByTransactionId")
    public BusinessTransactionPersistence findTransactionById(@RequestParam String transactionId, HttpServletRequest request, HttpServletResponse response) {

        BusinessTransactionPersistence tmp=null;

        List<BusinessTransactionPersistence> businessTransactionPersistences = indexerService.getBusinessTransactionPersistenceRepository().findAllByTransactionId(transactionId,PageRequest.of(0,5000));

        if(businessTransactionPersistences!=null) {
            for (BusinessTransactionPersistence b : businessTransactionPersistences) {

                if (tmp == null) {
                    tmp = b;
                }
                if (indexerService.toDate(tmp.getYear(), tmp.getMonth(), tmp.getDay(), tmp.getHour(), tmp.getMinute(), tmp.getMillis()).compareTo(
                        indexerService.toDate(b.getYear(), b.getMonth(), b.getDay(), b.getHour(), b.getMinute(), b.getMillis()))>0) {
                    tmp = b;
                }
            }
        }
        return tmp;
    }
    @GetMapping(value = "/findActionByBusinessId")
    public List<BusinessTransactionAction> findActionById(@RequestParam String businessId, HttpServletRequest request, HttpServletResponse response) {
        return indexerService.getBusinessTransactionActionRepository().findAllByBusinessTransactionId(businessId,Sort.by(new Sort.Order(Sort.Direction.ASC,"eventNumber")),PageRequest.of(0,5000));
    }

}
