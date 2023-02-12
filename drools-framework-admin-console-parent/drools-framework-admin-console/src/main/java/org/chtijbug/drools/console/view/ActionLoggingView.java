package org.chtijbug.drools.console.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helger.commons.csv.CSVWriter;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.io.IOUtils;
import org.chtijbug.drools.console.service.IndexerService;
import org.chtijbug.drools.console.service.util.AppContext;
import org.chtijbug.drools.console.vaadincomponent.componentperso.DialogPerso;
import org.chtijbug.drools.console.vaadincomponent.componentview.GridActionLogging;
import org.chtijbug.drools.logging.Fact;
import org.chtijbug.drools.proxy.persistence.model.BusinessTransactionAction;
import org.chtijbug.drools.proxy.persistence.model.BusinessTransactionPersistence;
import org.chtijbug.drools.proxy.persistence.model.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ActionLoggingView extends VerticalLayout {

    private static Logger logger = LoggerFactory.getLogger(ActionLoggingView.class);

    private transient IndexerService indexerService;

    private Label title;

    private GridActionLogging gridActionLogging;

    private String uniqueID;
    private String dbID;

    private ObjectMapper mapper = new ObjectMapper();

    public ActionLoggingView(BusinessTransactionPersistence businessTransactionPersistence, DialogPerso dialogPerso) {
        indexerService = AppContext.getApplicationContext().getBean(IndexerService.class);
        dialogPerso.getClose().setVisible(false);
        this.uniqueID = businessTransactionPersistence.getTransactionId();
        this.dbID = businessTransactionPersistence.getUniqueId().toString();
        if (this.uniqueID==null){
            this.uniqueID=this.dbID;
        }
        title = new Label("TransactionID : " + businessTransactionPersistence.getTransactionId() + "--" + businessTransactionPersistence.getUniqueId());
        title.setClassName("creation-runtime-title");

        add(title);


        gridActionLogging = new GridActionLogging(businessTransactionPersistence.getUniqueId().toString());
        Anchor anchor = new Anchor(new StreamResource(uniqueID.trim().toLowerCase()+".csv", this::getInputStream), "Export as CSV");
        anchor.getElement().setAttribute("download", true);
        add(gridActionLogging, anchor);
    }

    private InputStream getInputStream() {
        try {
            StringWriter stringWriter = new StringWriter();

            CSVWriter csvWriter = new CSVWriter(stringWriter);
            csvWriter.setSeparatorChar(';');
            csvWriter.writeNext("start", "end", "eventtype", "position", "rulename", "package", "ruleflowgroup", "processID", "IOData", "when", "then");
            List<BusinessTransactionAction> businessTransactionPersistences = indexerService.getBusinessTransactionActionRepository().findAllByBusinessTransactionId(dbID, Sort.by(new Sort.Order(Sort.Direction.ASC, "eventNumber")), PageRequest.of(0, 5000));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            for (BusinessTransactionAction c : businessTransactionPersistences) {

                String ruleName = "";
                String packageName = "";
                String beginDate = "";
                String endDate = "";
                String inputData = "";
                String outputData = "";
                if (c.getInputData() != null) {
                     inputData = mapper.writeValueAsString(c.getInputData().getRealFact());
                }
                if (c.getOutputData() != null) {
                     outputData = mapper.writeValueAsString(c.getOutputData().getRealFact());
                }
                if (c.getRuleExecution() != null) {
                    ruleName = c.getRuleExecution().getRuleName();
                    packageName = c.getRuleExecution().getPackageName();
                    beginDate = simpleDateFormat.format(c.getRuleExecution().getStartDate());
                    endDate = simpleDateFormat.format(c.getRuleExecution().getEndDate());
                }
                if (c.getEventType().equals(EventType.INPUT)) {
                    csvWriter.writeNext("", "", c.getEventType().name(), String.valueOf(c.getEventNumber()), "", "", "", "", "", "", "",c.getInputData().getFullClassName(),inputData);
                } else if (c.getEventType().equals(EventType.OUPUT)) {
                    csvWriter.writeNext("", "", c.getEventType().name(), String.valueOf(c.getEventNumber()), "", "", "", "", "", "", "",c.getOutputData().getFullClassName(),outputData);
                } else if (c.getEventType().equals(EventType.STARTPROCESS)) {
                    csvWriter.writeNext("", "", c.getEventType().name(), String.valueOf(c.getEventNumber()), "", "", "", "", "", "", "");
                } else if (c.getEventType().equals(EventType.STOPPROCESS)) {
                    csvWriter.writeNext("", "", c.getEventType().name(), String.valueOf(c.getEventNumber()), "", "", "", "", "", "", "");
                } else if (c.getEventType().equals(EventType.STARTRULEFLOWGROUP)
                        || c.getEventType().equals(EventType.STOPTRULEFLOWGROUP)) {
                    csvWriter.writeNext("", "", "" + c.getEventType().name(), String.valueOf(c.getEventNumber()), "", "", c.getRuleflowGroupName());
                } else if (c.getEventType().equals(EventType.STARTPROCESS)
                        || c.getEventType().equals(EventType.STOPPROCESS)) {
                    csvWriter.writeNext("", "", c.getEventType().name(), String.valueOf(c.getEventNumber()), "", "", "", c.getProcessID(), "", "", "");
                } else if (c.getEventType().equals(EventType.RULE)) {
                    List<String> datas = new ArrayList<>();
                    datas.add(simpleDateFormat.format(c.getRuleExecution().getStartDate()));
                    datas.add(simpleDateFormat.format(c.getRuleExecution().getEndDate()));
                    datas.add(c.getEventType().name());
                    datas.add(String.valueOf(c.getEventNumber()));
                    datas.add(c.getRuleExecution().getRuleName());
                    datas.add(c.getRuleExecution().getPackageName());
                    datas.add(c.getRuleflowGroupName());
                    datas.add(c.getProcessID());
                    datas.add("");
                    datas.add("");

                    for (Fact f : c.getRuleExecution().getWhenFacts()){
                        datas.add(f.getFullClassName());
                        datas.add(mapper.writeValueAsString(f.getRealFact()));
                    }
                    for (Fact f : c.getRuleExecution().getThenFacts()){
                        datas.add(f.getFullClassName());
                        datas.add(mapper.writeValueAsString(f.getRealFact()));
                    }
                     csvWriter.writeAll(Collections.singletonList(datas));
                } else {
                    csvWriter.writeNext(beginDate, endDate, c.getEventType().name(), String.valueOf(c.getEventNumber()),
                            ruleName, packageName, c.getRuleflowGroupName(), c.getProcessID()
                    );
                }
            }


            return IOUtils.toInputStream(stringWriter.toString(), StandardCharsets.UTF_8.name());

        } catch (IOException e) {
            logger.error("getInputStream.csvWriter",e);

            return null;
        }

    }

}
